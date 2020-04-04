package org.youdi.transformation


import org.apache.flink.streaming.api.scala._
import org.youdi.source.{MyCuseterSource, StationLog}

object SplitSelectDemo {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    val splitStreaam: SplitStream[StationLog] = stream.split(
      log => {
        if (log.duration > 5) {
          Seq(">5")
        } else {
          Seq("<=5")
        } // 给不同流打上不用标签
      }
    )

    val gt: DataStream[StationLog] = splitStreaam.select(">5") // 根据标签得到不同的流
    val lt: DataStream[StationLog] = splitStreaam.select("<=5") // 根据标签得到不同的流

    gt.print("gt")
    lt.print("lt")


    streamEnv.execute("split select")
  }
}
