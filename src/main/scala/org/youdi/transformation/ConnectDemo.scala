package org.youdi.transformation


import org.apache.flink.streaming.api.scala._

object ConnectDemo {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val stream1: DataStream[(String, Int)] = streamEnv.fromElements(("a", 1), ("b", 2), ("c", 3))
    val stream2: DataStream[String] = streamEnv.fromElements("e", "f", "g")
    val stream3: DataStream[(String, Int)] = streamEnv.fromElements(("d", 6), ("f", 10))


    // 将stream1 和 stream2放在一起，不是真正的合并
    val connStream: ConnectedStreams[(String, Int), String] = stream1.connect(stream2)

    val ds: DataStream[Object] = connStream.map(t => {
      (t._1, t._2)
    }, t => {
      (t, 1)
    })

    ds.print()
    streamEnv.execute("connect")

  }
}
