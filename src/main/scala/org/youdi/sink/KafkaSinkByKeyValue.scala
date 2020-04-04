package org.youdi.sink


import java.lang
import java.util.Properties

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaProducer, KafkaSerializationSchema}
import org.apache.kafka.clients.producer.ProducerRecord

object KafkaSinkByKeyValue {

  // kafka 采用 key value
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    streamEnv.setParallelism(1)

    val stream: DataStream[String] = streamEnv.socketTextStream("127.0.0.1", 8888)

    val result: DataStream[(String, Int)] = stream.flatMap(_.split(" "))
      .map((_, 1))
      .keyBy(0)
      .sum(1)


    val properties: Properties = new Properties()
    properties.setProperty("bootstrap.server", "host1:9092,host2:9092,host3:9092")


    val kafkaSink: FlinkKafkaProducer[(String, Int)] = new FlinkKafkaProducer[(String, Int)](
      "youdi",
      new KafkaSerializationSchema[(String, Int)] { // 自定义匿名内部类
        override def serialize(t: (String, Int), aLong: lang.Long) = {
          new ProducerRecord("youdi", t._1.getBytes, t._2.toString.getBytes)
        }
      },
      properties,
      FlinkKafkaProducer.Semantic.EXACTLY_ONCE
    )


    result.addSink(kafkaSink)

    streamEnv.execute("kafka key value")
  }
}
