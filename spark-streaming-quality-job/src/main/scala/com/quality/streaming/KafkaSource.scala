package com.quality.streaming

import org.apache.spark.sql.{DataFrame, SparkSession}

object KafkaSource {
  def createStream(spark: SparkSession, brokers: String, topic: String): DataFrame = {
    spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", brokers)
      .option("subscribe", topic)
      .option("startingOffsets", "latest")
      .option("failOnDataLoss", "false")
      .load()
      .selectExpr("CAST(value AS STRING) as json")
  }
}
