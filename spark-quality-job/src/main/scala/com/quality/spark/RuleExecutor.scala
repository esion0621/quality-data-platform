package com.quality.spark

import com.quality.spark.utils.JsonUtils
import org.apache.spark.sql.{DataFrame, SparkSession}
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

object RuleExecutor {
  private val logger = LoggerFactory.getLogger(getClass)

  def execute(spark: SparkSession, rule: Map[String, Any]): Map[String, Any] = {
    val sourceConfig = JsonUtils.parseMap(rule("sourceConfig").toString)
    val ruleParams = JsonUtils.parseMap(rule("ruleParams").toString)
    val ruleType = rule("ruleType").toString

    // 读取数据
    val path = sourceConfig.getOrElse("path", "").toString
    val format = sourceConfig.getOrElse("format", "parquet").toString
    logger.info(s"Reading data from $path with format $format")

    // 构建 DataFrameReader 并应用 options
    val reader = spark.read.format(format)
    sourceConfig.get("options").foreach { optsObj =>
      val opts = optsObj.asInstanceOf[Map[String, String]]
      opts.foreach { case (k, v) => reader.option(k, v) }
    }
    val df = reader.load(path)

    val total = df.count()
    val abnormal = ruleType match {
      case "NULL_CHECK" =>
        val column = ruleParams("column").toString
        val nullCount = df.filter(s"$column IS NULL").count()
        nullCount

      case "UNIQUE_CHECK" =>
        val columns = ruleParams("columns").toString.split(",").map(_.trim)
        val dupCount = df.groupBy(columns.head, columns.tail: _*).count().filter("count > 1").count()
        dupCount

      case "RANGE_CHECK" =>
        val column = ruleParams("column").toString
        val min = ruleParams("min").toString.toDouble
        val max = ruleParams("max").toString.toDouble
        df.filter(s"$column < $min OR $column > $max").count()

      case "SQL_EXPRESSION" =>
        val expr = ruleParams("expression").toString
        df.filter(expr).count()

      case _ =>
        logger.warn(s"Unknown rule type: $ruleType")
        0L
    }

    val rate = if (total == 0) 0.0 else abnormal.toDouble / total
    val threshold = ruleParams.getOrElse("threshold", "0.0").toString.toDouble
    val passed = rate <= threshold

    Map(
      "total" -> total,
      "abnormal" -> abnormal,
      "rate" -> rate,
      "passed" -> passed,
      "ruleType" -> ruleType,
      "threshold" -> threshold
    )
  }
}
