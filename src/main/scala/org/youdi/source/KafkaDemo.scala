package org.youdi.source

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
//import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011

object KafkaDemo {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val properties = new Properties()
    properties.setProperty("bootstrrap.servers", "localhost:9092")
    //    ....
//    val stream: DataStream[String] = env.addSource(new FlinkKafkaConsumer011[String]("sensor", new SimpleStringSchema(), properties))
//    stream.print("kafka").setParallelism(1)
    env.execute("kafka")

  }
}
