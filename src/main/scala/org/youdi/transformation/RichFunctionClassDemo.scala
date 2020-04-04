package org.youdi.transformation


import java.sql.{Connection, DriverManager, PreparedStatement, ResultSet}

import org.apache.flink.api.common.functions.RichMapFunction
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala._
import org.youdi.source.StationLog

object RichFunctionClassDemo {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val filePath: String = getClass.getResource("/station.log").getPath

    val result: DataStream[StationLog] = streamEnv.readTextFile(filePath).map(line => {
      val strings: Array[String] = line.split(",")
      new StationLog(strings(0).trim, strings(1).trim, strings(2).trim, strings(3).trim, strings(4).trim.toLong, strings(5).trim.toLong)
    })

    val stream: DataStream[StationLog] = result.filter(_.callType.equals("success")).map(new MyRichMapFunction)
    stream.print()

    streamEnv.execute("MyRichMapFunction")
  }
}

class MyRichMapFunction extends RichMapFunction[StationLog, StationLog] {
  var conn: Connection = _
  var post: PreparedStatement = _

  override def open(parameters: Configuration): Unit = {

    conn = DriverManager.getConnection("jdbc:mysql://localhost/test", "root", "root")
    post = conn.prepareStatement("select name from t_phone where phone_num=?")

  }

  override def map(value: StationLog): StationLog = {
    // 查询号码
    post.setString(1, value.callOut)
    val resultSet: ResultSet = post.executeQuery()
    if (resultSet.next()) {
      value.callOut = resultSet.getString(1)
    }
    // 查询


    // 注意样例类
    post.setString(1, value.callIn)
    val resultSet1: ResultSet = post.executeQuery()
    if (resultSet1.next()) {
      value.callIn = resultSet1.getString(1)
    }

    value
  }

  override def close(): Unit = {
    post.close()
    conn.close()
  }
}

