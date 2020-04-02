package org.youdi.sink

import org.apache.flink.api.common.serialization.{SimpleStringEncoder, SimpleStringSchema}
import org.apache.flink.core.fs.Path
import org.apache.flink.streaming.api.functions.sink.filesystem.StreamingFileSink
import org.apache.flink.streaming.api.functions.sink.filesystem.rollingpolicies.DefaultRollingPolicy
import org.apache.flink.streaming.api.scala._
import org.youdi.source.{MyCuseterSource, StationLog}


object HDFSSink {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    streamEnv.setParallelism(1)

    // 读取数据源
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)


    // 分桶策略 默认一个小时 设置滚动策略
    val roll: DefaultRollingPolicy[StationLog, String] = DefaultRollingPolicy.create()
      .withInactivityInterval(2000) // 不活动的分桶时间
      .withRolloverInterval(2000)
      .build()


    // 创建sink=
    val mySink: StreamingFileSink[StationLog] = StreamingFileSink.forRowFormat[StationLog](
      new Path("hdfs://host1:9000/jackson/sink"),
      new SimpleStringEncoder[StationLog]("UTF-8"))
      .withRollingPolicy(roll)
      .withBucketCheckInterval(1000) // 检查分桶时间间隔
      .build()

    stream.addSink(mySink)


    streamEnv.execute("sink hdfs")
  }
}
