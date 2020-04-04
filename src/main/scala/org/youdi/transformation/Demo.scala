package org.youdi.transformation


import org.apache.flink.streaming.api.scala._
import org.youdi.source.{MyCuseterSource, StationLog}

object Demo {

  // 从自定义的数据源中读取基站通话日志， 统计每个通话成功的时长是多少秒
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    val result: DataStream[(String, Long)] = stream.filter(_.callType.equals("success"))
      .map(log => {
        (log.sid, log.duration)
      })
      .keyBy(0)
      .reduce(
        (t1, t2) => {
          (t1._1, t1._2 + t2._2)
        }
      )
    result.print()


    streamEnv.execute("reduce")
  }
}
