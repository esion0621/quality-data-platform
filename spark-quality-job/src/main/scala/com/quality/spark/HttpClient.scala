package com.quality.spark

import com.quality.spark.utils.JsonUtils
import scalaj.http.{Http, HttpResponse}
import org.slf4j.LoggerFactory

object HttpClient {
  private val logger = LoggerFactory.getLogger(getClass)
  private val BACKEND_BASE = "http://192.168.237.10:2006" 

  def fetchRule(ruleId: Int): scala.collection.immutable.Map[String, Any] = {
    val url = s"$BACKEND_BASE/api/rules/$ruleId"
    try {
      val response: HttpResponse[String] = Http(url).timeout(5000, 10000).asString
      if (response.isSuccess) {
        JsonUtils.parseMap(response.body)
      } else {
        logger.error(s"Failed to fetch rule: ${response.code} ${response.body}")
        null
      }
    } catch {
      case e: Exception =>
        logger.error("Exception fetching rule", e)
        null
    }
  }

  def callback(instanceId: Long, result: Map[String, Any]): Boolean = {
    val url = s"$BACKEND_BASE/api/callback/spark-job"
    val body = JsonUtils.toJson(Map(
      "instanceId" -> instanceId,
      "status" -> (if (result.getOrElse("passed", false).asInstanceOf[Boolean]) "SUCCESS" else "FAILED"),
      "resultSummary" -> JsonUtils.toJson(result)
    ))
    try {
      val response = Http(url)
        .postData(body)
        .header("Content-Type", "application/json")
        .timeout(5000, 10000)
        .asString
      if (response.isSuccess) {
        logger.info(s"Callback success: ${response.body}")
        true
      } else {
        logger.error(s"Callback failed: ${response.code} ${response.body}")
        false
      }
    } catch {
      case e: Exception =>
        logger.error("Exception during callback", e)
        false
    }
  }
}
