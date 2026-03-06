package com.quality.streaming

import org.apache.spark.sql.api.java.UDF1
import org.apache.spark.sql.expressions.UserDefinedFunction
import org.apache.spark.sql.functions.udf
import org.apache.spark.sql.Row
import org.apache.spark.sql.types.{ArrayType, IntegerType}

import scala.collection.mutable.ArrayBuffer

object RuleExecutor {
  /**
   * 创建UDF，输入为一行数据的Row，输出触发的规则ID数组
   * 目前支持空值检测和范围检测（仅针对duration字段）
   */
  def createAnomalyUdf(rules: List[Rule]): UserDefinedFunction = {
    udf(new UDF1[Row, Array[Int]] {
      override def call(row: Row): Array[Int] = {
        val triggered = ArrayBuffer[Int]()
        // 获取duration值，可能为null
        val duration = if (row.isNullAt(row.fieldIndex("duration"))) null else row.getAs[Number]("duration")
        for (rule <- rules) {
          rule.ruleType match {
            case "NULL_CHECK" =>
              val column = rule.params.getOrElse("column", "duration").toString
              if (column == "duration" && duration == null) {
                triggered += rule.id
              }
            case "RANGE_CHECK" =>
              val column = rule.params.getOrElse("column", "duration").toString
              if (column == "duration" && duration != null) {
                val min = rule.params.getOrElse("min", 0).toString.toDouble
                val max = rule.params.getOrElse("max", Double.MaxValue).toString.toDouble
                val value = duration.doubleValue()
                if (value < min || value > max) {
                  triggered += rule.id
                }
              }
            // 其他规则类型可在此扩展
            case _ => // ignore
          }
        }
        triggered.toArray
      }
    }, ArrayType(IntegerType))
  }
}
