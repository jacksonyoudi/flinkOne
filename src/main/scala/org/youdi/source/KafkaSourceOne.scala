package org.youdi.source

import java.util.Properties

import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer
import org.apache.kafka.common.serialization.StringDeserializer

object KafkaSourceOne {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 2. 导入隐式转换
    import org.apache.flink.streaming.api.scala._


    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "host1:9092,host2:9092,host3:9092")
    properties.setProperty("group.id", "flink01")
    properties.setProperty("key.deserializer", classOf[StringDeserializer].getName)
    properties.setProperty("value.deserializer", classOf[StringDeserializer].getName)
    properties.setProperty("auto.offset.reset", "latest")

    val stream: DataStream[String] = streamEnv.addSource(new FlinkKafkaConsumer[String]("youdi", new SimpleStringSchema(), properties))


    stream.print()
    streamEnv.execute("kafka")
  }
}
