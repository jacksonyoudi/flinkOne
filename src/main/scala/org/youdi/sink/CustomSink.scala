package org.youdi.sink

import java.sql.{Connection, DriverManager, PreparedStatement}

import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.functions.sink.{RichSinkFunction, SinkFunction}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.youdi.source.{MyCuseterSource, StationLog}


/**
 * 自定义sink，
 * 1. 实现SinkFunction
 * 2. 实现RichSinkFunction 增加了生命周期的管理功能
 */
object CustomSink {
  // 将数据写到mysql
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    import org.apache.flink.streaming.api.scala._

    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    stream.addSink(new MyClusterJdbcSink)

    streamEnv.execute("mysql")

  }
}


class MyClusterJdbcSink extends RichSinkFunction[StationLog] {

  var conn: Connection = _
  var post: PreparedStatement = _

  // 写入数据， 一条执行一次
  override def invoke(value: StationLog, context: SinkFunction.Context[_]): Unit = {
    post.setString(1, value.sid)
    post.setString(2, value.callIn)
    post.setString(3, value.callOut)
    post.setString(4, value.callType)
    post.setLong(5, value.callTime)
    post.setLong(6, value.duration)

    post.executeUpdate()
  }


  // 打开，初始化
  override def open(parameters: Configuration): Unit = {
    conn = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "root")
    post = conn.prepareStatement("insert into station_log (sid, call_in, call_out, call_type, call_time, duration) values (?,?,?,?,?,?)")

  }

  override def close(): Unit = {
    post.close()
    conn.close()
  }
}
