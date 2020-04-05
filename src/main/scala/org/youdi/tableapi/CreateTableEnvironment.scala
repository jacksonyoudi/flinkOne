package org.youdi.tableapi

import org.apache.flink.api.scala.typeutils.Types
import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.{EnvironmentSettings, Table}
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.table.sources.CsvTableSource


object CreateTableEnvironment {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().inStreamingMode().build()
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(streamEnv, settings)

    // 创建表 静态(批)，动态
    val tableSource = new CsvTableSource("/station.log",
      Array[String]("f1", "f2", "f3", "f4", "f5", "f6"),
      Array(Types.STRING, Types.STRING, Types.STRING, Types.STRING, Types.LONG, Types.LONG))

    // 注册一张表
    tableEnv.registerTableSource("t_station_log", tableSource)

    // 可以使用sql Api
    //打印表 结构， 使用table APi， 需要得到table对象

    val table: Table = tableEnv.scan("t_station_log")
    table.printSchema()

  }
}
