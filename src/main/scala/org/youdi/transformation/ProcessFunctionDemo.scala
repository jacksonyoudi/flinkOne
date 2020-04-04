package org.youdi.transformation


import org.apache.flink.api.common.state.{ValueState, ValueStateDescriptor}
import org.apache.flink.streaming.api.functions.KeyedProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.youdi.source.StationLog

object ProcessFunctionDemo {
  // 监控所有的手机号码，在5秒内，所有呼叫它的日志，都是失败，则发出告警
  // 如果在5秒内只要一个呼叫不是fail，不用告警
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val stream: DataStream[String] = streamEnv.socketTextStream("127.0.0.1", 8888)
    val result: DataStream[StationLog] = stream.map(line => {
      val strings: Array[String] = line.split(",")
      new StationLog(strings(0).trim, strings(1).trim, strings(2).trim, strings(3).trim, strings(4).trim.toLong, strings(5).trim.toLong)
    })

    // 计算
    val ms: DataStream[String] = result.keyBy(_.callIn).process(new MonitiorCallFail)
    ms.print()

    streamEnv.execute()


  }
}


class MonitiorCallFail extends KeyedProcessFunction[String, StationLog, String] {
  // 使用状态记录时间
  lazy private val timeState: ValueState[Long] = getRuntimeContext.getState(new ValueStateDescriptor[Long]("time", classOf[Long]))


  // 时间到了， 定时器执行
  override def onTimer(timestamp: Long, ctx: KeyedProcessFunction[String, StationLog, String]#OnTimerContext, out: Collector[String]): Unit = {
    val warnString: String = "触发的时间:" + timestamp + " 手机号:" + ctx.getCurrentKey
    out.collect(warnString)
    timeState.clear()

  }

  override def processElement(value: StationLog, ctx: KeyedProcessFunction[String, StationLog, String]#Context, out: Collector[String]): Unit = {
    // 先从状态中获取时间
    val time: Long = timeState.value()
    if (time == 0 && value.callType.equals("fail")) {
      // 获取当前时间,并注册时间
      val nowTime: Long = ctx.timerService().currentProcessingTime()

      // 定时器在5秒后触发
      val onTime: Long = nowTime + 5 * 1000L

      ctx.timerService().registerEventTimeTimer(onTime)

      // 把触发时间保存在状态中
      timeState.update(onTime)
    }

    if (time != 0 && !value.callType.equals("fail")) { // 表示有一次成功的呼叫
      ctx.timerService().deleteEventTimeTimer(time)
      // 清空
      timeState.clear()
    }


  }
}