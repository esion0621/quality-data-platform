package com.quality.spark.utils

import org.apache.spark.sql.SparkSession

object SparkUtils {
  def createSparkSession(appName: String): SparkSession = {
    SparkSession.builder()
      .appName(appName)
      .config("spark.sql.adaptive.enabled", "true")
      .config("spark.sql.adaptive.coalescePartitions.enabled", "true")
      .getOrCreate()
  }
}
