package com.quality.streaming.utils

import scalaj.http.{Http, HttpResponse}

object HttpClient {
  def get(url: String, timeout: Int = 5000): Option[String] = {
    try {
      val response: HttpResponse[String] = Http(url).timeout(timeout, timeout).asString
      if (response.isSuccess) Some(response.body) else None
    } catch {
      case e: Exception => None
    }
  }

  def postJson(url: String, json: String, timeout: Int = 5000): Boolean = {
    try {
      val response = Http(url)
        .postData(json)
        .header("Content-Type", "application/json")
        .timeout(timeout, timeout)
        .asString
      response.isSuccess
    } catch {
      case e: Exception =>
        println(s"HTTP POST failed: ${e.getMessage}")
        false
    }
  }
}
