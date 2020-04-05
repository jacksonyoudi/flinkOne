package org.youdi.tableapi

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api._
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.youdi.source.{MyCuseterSource, StationLog}


object CreateTableByStream {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().inStreamingMode().build()
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(streamEnv, settings)

    // 创建表 静态(批)，动态

    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    // 注册表
    tableEnv.registerDataStream("t_2", stream)

    tableEnv.sqlQuery("select * from t_2").printSchema()

    //    val table: Table = tableEnv.scan("t_2")
    //
    //    table.printSchema()

    streamEnv.execute()
  }
}
