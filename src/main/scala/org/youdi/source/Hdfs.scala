package org.youdi.source

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object Hdfs {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 2. 导入隐式转换
    import org.apache.flink.streaming.api.scala._

    val ds: DataStream[String] = streamEnv.readTextFile("hdfs://host1:9000/jackson/wordcount/input")

    val dsOne: DataStream[(String, Int)] = ds.flatMap(_.split(" "))
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    // sink
    dsOne.print()

    streamEnv.execute("hdfs")


  }
}
