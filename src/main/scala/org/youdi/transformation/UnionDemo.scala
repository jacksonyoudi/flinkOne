package org.youdi.transformation

import org.apache.flink.streaming.api.scala._

object UnionDemo {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val stream1: DataStream[(String, Int)] = streamEnv.fromElements(("a", 1), ("b", 2))
    val stream2: DataStream[(String, Int)] = streamEnv.fromElements(("b", 3), ("c", 5))
    val stream3: DataStream[(String, Int)] = streamEnv.fromElements(("d", 6), ("f", 10))


    val result: DataStream[(String, Int)] = stream1.union(stream2).union(stream3)

    result.print()

    streamEnv.execute("union")
  }
}
