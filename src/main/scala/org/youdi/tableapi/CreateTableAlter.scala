package org.youdi.tableapi

import org.apache.flink.streaming.api.scala._
import org.apache.flink.table.api.EnvironmentSettings
import org.apache.flink.table.api.scala.StreamTableEnvironment
import org.apache.flink.types.Row
import org.youdi.source.{MyCuseterSource, StationLog}


object CreateTableAlter {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().inStreamingMode().build()
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(streamEnv, settings)

    // 创建表 静态(批)，动态

    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    // 注册表
    //    tableEnv.registerDataStream("t_2", stream)

    //    tableEnv.sqlQuery("select * from t_2").printSchema()

    //    val table: Table = tableEnv.scan("t_2")
    //
    //    table.printSchema()

    import org.apache.flink.streaming.api.scala._
    import org.apache.flink.table.api._
    // 修改字段名字
    val table: Table = tableEnv.fromDataStream(stream)
    //      tableEnv.fromDataStream(stream, ')
    //    table.printSchema()

    // 查询过滤
    //    val result: Any = table.filter('callType == "success")
    //    val result: Table = table.filter("duration>4")

    //    val ds: DataStream[Row] = tableEnv.toAppendStream[Row](result)
    val result: Table = table.groupBy("sid").select("sid,count(sid) as cnt")
    // 5> (true,0012,5)
    //6> (true,0018,14)
    //1> (true,0016,12)
    val ds: DataStream[(Boolean, Row)] = tableEnv.toRetractStream[Row](result).filter(_._1 == true)

    ds.print()

    streamEnv.execute()
  }
}
