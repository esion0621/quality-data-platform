package com.quality.spark

import com.quality.spark.utils.{JsonUtils, SparkUtils}
import org.apache.spark.sql.SparkSession
import org.slf4j.LoggerFactory

object QualityJob {
  private val logger = LoggerFactory.getLogger(getClass)

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      logger.error("Usage: QualityJob <ruleId> <instanceId>")
      System.exit(1)
    }
    val ruleId = args(0).toInt
    val instanceId = args(1).toLong
    logger.info(s"Starting quality job for ruleId=$ruleId, instanceId=$instanceId")

    // 1. 获取规则详情
    val rule = HttpClient.fetchRule(ruleId)
    if (rule == null) {
      logger.error(s"Failed to fetch rule $ruleId")
      System.exit(2)
    }

    // 2. 创建 SparkSession
    val spark = SparkUtils.createSparkSession(s"QualityJob-Rule$ruleId")

    try {
      // 3. 执行规则校验
      val result = RuleExecutor.execute(spark, rule)

      // 4. 回调后端
      val success = HttpClient.callback(instanceId, result)
      if (success) {
        logger.info(s"Callback succeeded for instance $instanceId")
      } else {
        logger.error(s"Callback failed for instance $instanceId")
      }
    } catch {
      case e: Exception =>
        logger.error("Error during quality check", e)
        // 尝试回调失败状态
        val errorResult = Map("error" -> e.getMessage)
        HttpClient.callback(instanceId, errorResult)
        System.exit(3)
    } finally {
      spark.stop()
    }
  }
}
