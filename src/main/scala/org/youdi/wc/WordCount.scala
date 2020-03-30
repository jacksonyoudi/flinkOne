package org.youdi.wc

import org.apache.flink.api.scala._


object WordCount {
  def main(args: Array[String]): Unit = {
    // 创建一个批处理执行环境
    val env: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment

    // 从文件中读取数据
    val inputDataSet: DataSet[String] = env.readTextFile("./input")

    // 分词
    val agg: AggregateDataSet[(String, Int)] = inputDataSet.flatMap(_.split(" ")).map((_, 1)).groupBy(0).sum(1)

    agg.print()


  }
}
