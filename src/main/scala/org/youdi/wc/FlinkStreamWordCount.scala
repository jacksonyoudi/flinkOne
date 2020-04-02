package org.youdi.wc


import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

object FlinkStreamWordCount {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 2. 导入隐式转换
    import org.apache.flink.streaming.api.scala._

    // 读取数据，读取socket数据流
    val stream: DataStream[String] = env.socketTextStream("10.211.55.5", 9999) // 等同spark中的 Dstream

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
