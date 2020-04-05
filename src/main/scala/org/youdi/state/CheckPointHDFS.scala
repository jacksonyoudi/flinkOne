package org.youdi.state

import org.apache.flink.runtime.state.filesystem.FsStateBackend
import org.apache.flink.streaming.api.CheckpointingMode
import org.apache.flink.streaming.api.environment.CheckpointConfig.ExternalizedCheckpointCleanup
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object CheckPointHDFS {
  //
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 开启checkpoint
    env.enableCheckpointing(5000) // 5s开启一次

    env.setStateBackend(new FsStateBackend("hdfs://host1:9000/jackson/sink/checkpoint")) // 存放检查点数据

    env.getCheckpointConfig.setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE)
    env.getCheckpointConfig.setCheckpointTimeout(5000)
    env.getCheckpointConfig.setMaxConcurrentCheckpoints(1)
    env.getCheckpointConfig.enableExternalizedCheckpoints(ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION) // 终止job，保留数据


    // 2. 导入隐式转换
    import org.apache.flink.streaming.api.scala._

    // 读取数据，读取socket数据流
    val stream: DataStream[String] = env.socketTextStream("127.0.0.1", 9999) // 等同spark中的 Dstream

    // 转化
    val result: DataStream[(String, Int)] = stream.flatMap(_.split(" "))
      .map((_, 1))
      .keyBy(0) // 分组
      .sum(1) // 聚合累加算子

    // 打印结果
    result.print("结果")

    // 启动流计算程序
    env.execute("stream wordcount")
  }
}
