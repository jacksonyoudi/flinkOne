package org.youdi.window

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.youdi.source.{MyCuseterSource, StationLog}

object ReduceFunctionbyWindowDemo {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 开窗
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)


    val result: DataStream[(String, Int)] = stream.map((log) => (log.sid, 1))
      .keyBy(_._1)
      .timeWindow(Time.seconds(5))
      .reduce((t1, t2) => (t1._1, t1._2 + t2._2)) // 类型要一致


    result.print()
    // 启动流计算程序
    streamEnv.execute("stream  window word count")
  }
}
