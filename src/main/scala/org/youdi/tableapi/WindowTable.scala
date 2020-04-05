package org.youdi.tableapi

import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.apache.flink.table.api._
import org.apache.flink.table.descriptors.{Kafka, Rowtime, Schema}
import org.youdi.source.{MyCuseterSource, StationLog}

object WindowTable {
  def main(args: Array[String]): Unit = {

    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().inStreamingMode().build()
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(streamEnv, settings)

    // 创建表 静态(批)，动态
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    //
    val table: Table = tableEnv.fromDataStream(stream)

    // 开窗
    val result: Table = table.window(Tumble.over("5.second").on("callTime").as("window"))
      .groupBy("window,sid")
      .select("window,sid")
    //    table.window(Tumble over 5.second on 'callTime as 'window)
    //    table.window(Slide.over("10.second").every("5.second").on("callTime").as("window"))

    val ds: DataStream[(Boolean, Row)] = tableEnv.toRetractStream[Row](result)

    ds.print()

    streamEnv.execute()
  }
}
