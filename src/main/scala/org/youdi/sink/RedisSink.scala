package org.youdi.sink

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.connectors.redis.RedisSink
import org.apache.flink.streaming.connectors.redis.common.config.FlinkJedisPoolConfig
import org.apache.flink.streaming.connectors.redis.common.mapper.{RedisCommand, RedisCommandDescription, RedisMapper}

object RedisSink {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    streamEnv.setParallelism(1)

    val stream: DataStream[String] = streamEnv.socketTextStream("127.0.0.1", 8888)

    val result: DataStream[(String, Int)] = stream.flatMap(_.split(" "))
      .map((_, 1))
      .keyBy(0)
      .sum(1)

    val config: FlinkJedisPoolConfig = new FlinkJedisPoolConfig.Builder().setDatabase(0).setHost("127.0.0.1").setPort(6379).build()


    result.addSink(new RedisSink[(String, Int)](config, new RedisMapper[(String, Int)] {
      override def getCommandDescription = {
        new RedisCommandDescription(RedisCommand.HSET, "flink")
      }

      override def getKeyFromData(t: (String, Int)) = {
        t._1
      }

      override def getValueFromData(t: (String, Int)) = {
        t._2.toString
      }
    }))

    streamEnv.execute("redis")

  }
}
