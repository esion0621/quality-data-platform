package com.quality.streaming

import org.apache.spark.sql.{DataFrame, SparkSession}
import org.apache.spark.sql.streaming.Trigger
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import com.quality.streaming.utils.{JsonUtils, HttpClient}
import org.apache.hadoop.hbase.{HBaseConfiguration, TableName}
import org.apache.hadoop.hbase.client.{Connection, ConnectionFactory, Put, Admin}
import org.apache.hadoop.hbase.util.Bytes
import java.net.URLEncoder
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import java.nio.charset.StandardCharsets
import java.util.Base64

object StreamingQualityJob {
  val DINGTALK_WEBHOOK = ""
  val DINGTALK_SECRET = ""

  // HBase 连接配置（建议使用连接池或单例）
  @transient private lazy val hbaseConnection: Connection = {
    val conf = HBaseConfiguration.create()
    conf.set("hbase.zookeeper.quorum", "master,slave1,slave2")
    conf.set("hbase.zookeeper.property.clientPort", "2181")
    val conn = ConnectionFactory.createConnection(conf)
    // 检查表是否存在，不存在则创建
    try {
      val admin = conn.getAdmin
      val tableName = TableName.valueOf("realtime_alert")
      if (!admin.tableExists(tableName)) {
        println("HBase table 'realtime_alert' does not exist, creating...")
        val tableDesc = org.apache.hadoop.hbase.client.TableDescriptorBuilder.newBuilder(tableName)
          .setColumnFamily(org.apache.hadoop.hbase.client.ColumnFamilyDescriptorBuilder.of("cf"))
          .build()
        admin.createTable(tableDesc)
        println("Table created.")
      }
      admin.close()
    } catch {
      case e: Exception =>
        println(s"Error checking/creating HBase table: ${e.getMessage}")
        e.printStackTrace()
    }
    conn
  }

  def generateDingTalkSign(timestamp: Long): String = {
    val stringToSign = timestamp + "\n" + DINGTALK_SECRET
    val mac = Mac.getInstance("HmacSHA256")
    mac.init(new SecretKeySpec(DINGTALK_SECRET.getBytes(StandardCharsets.UTF_8), "HmacSHA256"))
    val signData = mac.doFinal(stringToSign.getBytes(StandardCharsets.UTF_8))
    val sign = Base64.getEncoder.encodeToString(signData)
    URLEncoder.encode(sign, StandardCharsets.UTF_8.name())
  }

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("Usage: StreamingQualityJob <kafkaBrokers> <topic>")
      System.exit(1)
    }
    val kafkaBrokers = args(0)
    val topic = args(1)

    val spark = SparkSession.builder()
      .appName("StreamingQualityJob")
      .config("spark.sql.shuffle.partitions", "10")
      .getOrCreate()

    import spark.implicits._

    // 1. 获取实时规则
    val rules = RuleFetcher.fetchRealtimeRules()
    if (rules.isEmpty) {
      println("No realtime rules found, exiting.")
      spark.stop()
      System.exit(0)
    }
    println(s"Loaded ${rules.size} rules: ${rules.map(_.id).mkString(", ")}")
    val rulesBroadcast = spark.sparkContext.broadcast(rules)

    // 2. 创建Kafka流
    val kafkaDF = KafkaSource.createStream(spark, kafkaBrokers, topic)

    // 3. 解析JSON并添加时间戳
    val parsed = kafkaDF.selectExpr("json")
      .as[String]
      .map { json =>
        val map = JsonUtils.parseMap(json)
        val userId = map.get("user_id").map(_.toString.toInt).map(Integer.valueOf).orNull
        val page = map.get("page").map(_.toString).orNull
        val duration = map.get("duration").map {
          case null => null
          case x => Integer.valueOf(x.toString.toInt)
        }.orNull
        val timestamp = map.get("timestamp").map(_.toString).orNull
        (userId, page, duration, timestamp)
      }
      .toDF("user_id", "page", "duration", "timestamp")
      .withColumn("timestamp", to_timestamp($"timestamp"))

    // 4. 定义UDF
    val anomalyUdf = RuleExecutor.createAnomalyUdf(rules)

    // 5. 设置watermark
    val withWatermark = parsed.withWatermark("timestamp", "10 seconds")

    // 6. 流处理逻辑
    val query = withWatermark
      .writeStream
      .foreachBatch { (batchDF: DataFrame, batchId: Long) =>
        import batchDF.sparkSession.implicits._
        val rules = rulesBroadcast.value

        val withWindow = batchDF
          .withColumn("window", window($"timestamp", "1 minute"))
          .withColumn("anomaly_rules", anomalyUdf(struct($"*")))

        val totalCounts = withWindow.groupBy($"window").count()
        val anomalyCounts = withWindow
          .select($"window", explode($"anomaly_rules").as("rule_id"))
          .groupBy($"window", $"rule_id")
          .count()
          .withColumnRenamed("count", "anomaly_count")

        val joined = totalCounts
          .join(anomalyCounts, Seq("window"), "left_outer")
          .na.fill(0, Seq("anomaly_count"))

        joined.collect().foreach { row =>
          val window = row.getStruct(0)
          val windowEnd = window.getTimestamp(1).getTime
          val ruleId = row.getAs[Int]("rule_id")
          val total = row.getAs[Long]("count")
          val anomaly = row.getAs[Long]("anomaly_count")
          val rate = if (total == 0) 0.0 else anomaly.toDouble / total

          // 写入 Redis
          val key = s"realtime:rule:$ruleId"
          val field = windowEnd.toString
          val value = JsonUtils.toJson(Map(
            "total" -> total,
            "anomaly" -> anomaly,
            "rate" -> rate,
            "windowEnd" -> windowEnd
          ))
          val jedis = RedisSink.getJedis
          try {
            jedis.hset(key, field, value)
            jedis.hset(s"$key:latest", "latest", value)
            jedis.expire(key, 3600L)
          } finally {
            RedisSink.returnJedis(jedis)
          }

          // 告警判断
          val rule = rules.find(_.id == ruleId).orNull
          if (rule != null) {
            val threshold = rule.params.getOrElse("threshold", 0.0).toString.toDouble
            if (rate > threshold) {
              // 1. 发送钉钉直接告警
              try {
                val timestamp = System.currentTimeMillis
                val sign = generateDingTalkSign(timestamp)
                val signedUrl = s"$DINGTALK_WEBHOOK&timestamp=$timestamp&sign=$sign"
                val dingtalkJson = JsonUtils.toJson(Map(
                  "msgtype" -> "text",
                  "text" -> Map("content" -> s"实时告警：规则 $ruleId 异常率 $rate 超过阈值 $threshold")
                ))
                val dingtalkSuccess = HttpClient.postJson(signedUrl, dingtalkJson)
                if (dingtalkSuccess) {
                  println(s"[DingTalk] Alert triggered for rule $ruleId, rate=$rate")
                } else {
                  println(s"[DingTalk] Failed to send alert for rule $ruleId")
                }
              } catch {
                case e: Exception =>
                  println(s"[DingTalk] Error: ${e.getMessage}")
                  e.printStackTrace()
              }

              // 2. 写入 HBase（使用共享连接）
              try {
                val table = hbaseConnection.getTable(TableName.valueOf("realtime_alert"))
                val rowKey = s"${ruleId}_${windowEnd}_${System.currentTimeMillis() % 1000}"
                val put = new Put(Bytes.toBytes(rowKey))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("ruleId"), Bytes.toBytes(ruleId.toString))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("rate"), Bytes.toBytes(rate.toString))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("threshold"), Bytes.toBytes(threshold.toString))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("total"), Bytes.toBytes(total.toString))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("anomaly"), Bytes.toBytes(anomaly.toString))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("windowEnd"), Bytes.toBytes(windowEnd.toString))
                put.addColumn(Bytes.toBytes("cf"), Bytes.toBytes("timestamp"), Bytes.toBytes(System.currentTimeMillis.toString))

                table.put(put)
                table.close()
                println(s"[HBase] Alert written for rule $ruleId, rowKey=$rowKey")
              } catch {
                case e: Exception =>
                  println(s"[HBase] Error writing for rule $ruleId: ${e.getMessage}")
                  e.printStackTrace() // 打印完整堆栈
              }
            }
          }
        }
      }
      .outputMode("update")
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .start()

    query.awaitTermination()
  }
}
