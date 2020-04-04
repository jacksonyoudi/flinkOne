package org.youdi.state

import org.apache.flink.api.common.functions.RichFlatMapFunction
import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.configuration.Configuration
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.youdi.source.{MyCuseterSource, StationLog}

/**
 * 统计每个手机的呼叫间隔，单位毫秒
 */
object KeyedStateDemo1 {

  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)
    val result: DataStream[(String, Long)] = stream.keyBy(_.callOut) // 分组
      .flatMap(new CallIntervalFunction)

    result.print()

    streamEnv.execute()


  }
}

// 输出的是一个二元组
class CallIntervalFunction extends RichFlatMapFunction[StationLog, (String, Long)] {
  //定义一个状态，用于保存前一次的呼叫时间
  private var preCallTimeState: ValueState[Long] = _


  override def open(parameters: Configuration): Unit = {
    preCallTimeState = getRuntimeContext.getState(new ValueStateDescriptor[Long]("pre", classOf[Long]))
  }

  override def flatMap(value: StationLog, out: Collector[(String, Long)]): Unit = {
    // 从状态中取得上一次呼叫的时间
    val pre: Long = preCallTimeState.value()
    if (pre == null || pre == 0) {
      preCallTimeState.update(value.callTime)
    } else {
      val dur: Long = value.callTime - pre

      out.collect((value.callOut, dur))
      preCallTimeState.update(value.callTime)
    }

  }
}