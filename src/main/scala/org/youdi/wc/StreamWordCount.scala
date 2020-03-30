package org.youdi.wc

import org.apache.flink.api.java.utils.ParameterTool
import org.apache.flink.streaming.api.scala._

object StreamWordCount {
  def main(args: Array[String]): Unit = {

    val parameters: ParameterTool = ParameterTool.fromArgs(args)
    val host: String = parameters.get("host")
    val port: Int = parameters.getInt("port")


    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    // 接收socket数据流
    val textDataStream: DataStream[String] = env.socketTextStream("localhost", 7777)

    val wordCountStream: DataStream[(String, Int)] = textDataStream.flatMap(_.split(" "))
      .filter(_.nonEmpty)
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    wordCountStream.print().setParallelism(1)

    // 打印输出
    env.execute("stream word count job")

    //    6> (youdi,1) 线程数据
    //1> (nihao,1)
  }
}
