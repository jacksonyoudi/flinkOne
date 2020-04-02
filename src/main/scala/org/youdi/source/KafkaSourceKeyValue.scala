package org.youdi.source

import java.util.Properties

import org.apache.flink.api.common.typeinfo.TypeInformation
import org.apache.flink.streaming.connectors.kafka.{FlinkKafkaConsumer, KafkaDeserializationSchema}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.flink.streaming.api.scala._

object KafkaSourceKeyValue {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 2. 导入隐式转换

    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "host1:9092,host2:9092,host3:9092")
    properties.setProperty("group.id", "flink02")
    properties.setProperty("key.deserializer", classOf[StringDeserializer].getName)
    properties.setProperty("value.deserializer", classOf[StringDeserializer].getName)
    properties.setProperty("auto.offset.reset", "latest")

    // 设置数据源
    val stream: DataStream[(String, String)] = streamEnv.addSource(new FlinkKafkaConsumer[(String, String)]("youdi", new MyKafkaReader, properties))
    stream.print()
    streamEnv.execute("kafka")
  }

  // 定义一个类 从 kafka中读取键值对的数据
  class MyKafkaReader extends KafkaDeserializationSchema[(String, String)] {
    // 是否流结束
    override def isEndOfStream(t: (String, String)): Boolean = {
      false
    }

    // 反序列化
    override def deserialize(consumerRecord: ConsumerRecord[Array[Byte], Array[Byte]]): (String, String) = {
      if (consumerRecord != null) {
        var key = "null"
        var value = "null"
        if (consumerRecord.key() != null) {
          key = new String(consumerRecord.key(), "UTF-8")
        }

        if (consumerRecord.value() != null) {
          value = new String(consumerRecord.value(), "UTF-8")
        }

        (key, value)

      } else {

        ("null", "null")
      }
    }

    // 指定类型
    override def getProducedType: TypeInformation[(String, String)] = {
      createTuple2TypeInformation(createTypeInformation[String], createTypeInformation[String])
    }
  }

}
