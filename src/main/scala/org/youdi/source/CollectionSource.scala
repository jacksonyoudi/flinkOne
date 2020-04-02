package org.youdi.source

import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment


/**
 * 基站日志
 *
 * @param sid     id
 * @param callOut 主叫号码
 * @param callIn  被叫号码
 * @param callType
 * @param callTime
 * @param duration
 */
case class StationLog(sid: String, callOut: String, callIn: String, callType: String, callTime: Long, duration: Long)

object CollectionSource {

  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 设置并行度
    streamEnv.setParallelism(1)

    // 隐式转换
    import org.apache.flink.streaming.api.scala._
    val stream: DataStream[StationLog] = streamEnv.fromCollection(Array(
      new StationLog("001", "186", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("002", "187", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("003", "189", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("004", "1810", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("005", "1811", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("006", "1812", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("007", "1813", "187", "busy", System.currentTimeMillis(), 0),
      new StationLog("008", "1814", "187", "success", System.currentTimeMillis(), 20)))

    stream.print()
    streamEnv.execute("collection")
  }
}
