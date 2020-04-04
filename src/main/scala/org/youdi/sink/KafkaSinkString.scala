package org.youdi.sink

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaProducer

object KafkaSinkString {

  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamEnv.setParallelism(1)

    val stream: DataStream[String] = streamEnv.socketTextStream("127.0.0.1", 8888)

    val result: DataStream[String] = stream.flatMap(_.split(" "))

    result.addSink(new FlinkKafkaProducer[String]("host1:9092,host2:9092,host3:9092", "flink", new SimpleStringSchema()))

    streamEnv.execute("kafka string")
  }

}
