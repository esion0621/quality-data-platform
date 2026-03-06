package com.quality.spark.utils

import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper
import scala.collection.immutable.Map

object JsonUtils {
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(obj: Any): String = {
    mapper.writeValueAsString(obj)
  }

  def parseMap(json: String): Map[String, Any] = {
    mapper.readValue[Map[String, Any]](json)
  }
}
