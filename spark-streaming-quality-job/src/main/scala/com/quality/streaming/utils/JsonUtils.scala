package com.quality.streaming.utils

import com.fasterxml.jackson.core.`type`.TypeReference
import com.fasterxml.jackson.databind.{DeserializationFeature, ObjectMapper}
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.fasterxml.jackson.module.scala.experimental.ScalaObjectMapper

import scala.collection.immutable.{Map => ImmutableMap}

object JsonUtils {
  private val mapper = new ObjectMapper() with ScalaObjectMapper
  mapper.registerModule(DefaultScalaModule)
  mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

  def toJson(obj: Any): String = {
    mapper.writeValueAsString(obj)
  }

  def parseMap(json: String): ImmutableMap[String, Any] = {
    mapper.readValue[ImmutableMap[String, Any]](json)
  }

  def parseList(json: String): List[ImmutableMap[String, Any]] = {
    mapper.readValue(json, new TypeReference[List[ImmutableMap[String, Any]]] {})
  }

  def parse[T](json: String, clazz: Class[T]): T = {
    mapper.readValue(json, clazz)
  }
}
