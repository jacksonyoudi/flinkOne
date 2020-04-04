package org.youdi.transformation

import org.apache.flink.api.common.functions.MapFunction
import org.apache.flink.streaming.api.scala._
import org.youdi.source.StationLog

object FunctionClassDemo {

  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val filePath: String = getClass.getResource("/station.log").getPath

    val result: DataStream[StationLog] = streamEnv.readTextFile(filePath).map(line => {
      val strings: Array[String] = line.split(",")
      new StationLog(strings(0).trim, strings(1).trim, strings(2).trim, strings(3).trim, strings(4).trim.toLong, strings(5).trim.toLong)
    })


    val stream: DataStream[String] = result.filter(_.callType.equals("success"))
      .map(new MyMapFunction)

    stream.print()

    streamEnv.execute()

  }
}


//自定义函数类
class MyMapFunction extends MapFunction[StationLog, String] {
  override def map(t: StationLog): String = {
    "号码:" + t.sid + t.callOut + t.callIn + "结束时间" + t.callTime + t.duration
  }
}
