package org.youdi.state

import org.apache.flink.streaming.api.scala._
import org.youdi.source.{MyCuseterSource, StationLog}

/**
 * 统计每个手机的呼叫间隔，单位毫秒
 */
object KeyedStateDemo2 {

  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)
    val result: DataStream[(String, Long)] = stream.keyBy(_.callOut) // 分组
      // 有两种情况，状态中有上一次时间， 没有时间
      .mapWithState[(String, Long), StationLog] {
        case (in: StationLog, None) => ((in.callOut, 0), Some(in))
        case (in: StationLog, pre: Some[StationLog]) => {
          ((in.callOut, in.callTime - pre.get.callTime), Some(in))
        }
      }.filter(_._2 != 0)

    result.print()

    streamEnv.execute()


  }
}