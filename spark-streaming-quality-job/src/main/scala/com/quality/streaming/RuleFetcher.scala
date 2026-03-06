package com.quality.streaming

import com.quality.streaming.utils.{HttpClient, JsonUtils}
import scala.collection.immutable.{Map => ImmutableMap}

case class Rule(id: Int, ruleType: String, params: ImmutableMap[String, Any])

object RuleFetcher {
  private val backendUrl = "http://master:2006" 

  def fetchRealtimeRules(): List[Rule] = {
    val url = s"$backendUrl/api/rules/realtime"
    HttpClient.get(url) match {
      case Some(json) =>
        try {
          val list = JsonUtils.parseList(json)
          list.map { m =>
            val id = m("id").asInstanceOf[Int]
            val ruleType = m("ruleType").asInstanceOf[String]
            val params = JsonUtils.parseMap(m("ruleParams").toString)
            Rule(id, ruleType, params)
          }
        } catch {
          case e: Exception =>
            println(s"Failed to parse rules: ${e.getMessage}")
            Nil
        }
      case None =>
        println("Failed to fetch rules from backend")
        Nil
    }
  }
}
