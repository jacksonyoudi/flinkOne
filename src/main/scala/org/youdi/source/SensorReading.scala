package org.youdi.source

import org.apache.flink.streaming.api.scala._

case class SensorReading(id: String, timestamp: Long, temperature: Double)

object SourceTest {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 1. 从自定义的集合中读取数据
    val stream: DataStream[SensorReading] = env.fromCollection(List(
      SensorReading("one", 111, 35.0),
      SensorReading("two", 112, 35.0),
      SensorReading("three", 112, 35.0),
      SensorReading("four", 113, 35.0),
      SensorReading("five", 114, 35.0),
      SensorReading("six", 115, 35.0)
    ))

    stream.print("stream").setParallelism(6)
    env.execute("temperature sensor")

  }
}
