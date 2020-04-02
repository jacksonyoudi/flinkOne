package org.youdi.source

import java.util.Properties

import org.apache.kafka.common.serialization.StringSerializer
import org.apache.kafka.clients.producer.{KafkaProducer, ProducerRecord}

object MyKafkaProducer {
  def main(args: Array[String]): Unit = {
    val properties = new Properties()
    properties.setProperty("bootstrap.servers", "host1:9092,host2:9092,host3:9092")
    properties.setProperty("key.serializer", classOf[StringSerializer].getName)
    properties.setProperty("value.serializer", classOf[StringSerializer].getName)

    val pd: KafkaProducer[String, String] = new KafkaProducer[String, String](properties)

    while (true) {
      val value: ProducerRecord[String, String] = new ProducerRecord[String, String]("youdi", "key", "value")
      pd.send(value)
      Thread.sleep(1000)
    }

    pd.close()

  }
}
