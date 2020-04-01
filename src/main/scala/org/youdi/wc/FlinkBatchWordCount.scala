package org.youdi.wc

import org.apache.flink.api.scala.{DataSet, ExecutionEnvironment}

object FlinkBatchWordCount {
  def main(args: Array[String]): Unit = {
    val batchEnv: ExecutionEnvironment = ExecutionEnvironment.getExecutionEnvironment


    //    getClass.getResource("input")

    val ds: DataSet[String] = batchEnv.readTextFile("input")

    import org.apache.flink.api.scala._
    val ads: AggregateDataSet[(String, Int)] = ds.flatMap(_.split(" "))
      .map((_, 1))
      .groupBy(0)
      .sum(1)

    ads.print()
    //    batch 不用启动
  }
}
