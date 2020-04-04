package org.youdi.transformation

import org.apache.flink.streaming.api.functions.ProcessFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.util.Collector
import org.youdi.source.StationLog

object SideOutStreamDemo {
  var failTag = new OutputTag[StationLog]("fail")


  // 把呼叫成功的日志输出到主流，不成功的输出到侧流
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val filePath: String = getClass.getResource("/station.log").getPath

    val result: DataStream[StationLog] = streamEnv.readTextFile(filePath).map(line => {
      val strings: Array[String] = line.split(",")
      new StationLog(strings(0).trim, strings(1).trim, strings(2).trim, strings(3).trim, strings(4).trim.toLong, strings(5).trim.toLong)
    })
    val stream: DataStream[StationLog] = result
      .process(new CreaeteSideStream(failTag))

    stream.print("主流")
    // 一定根据主流得到侧流
    val failStream: DataStream[StationLog] = stream.getSideOutput(failTag)
    failStream.print("侧流")

    streamEnv.execute()
  }
}


class CreaeteSideStream(tag: OutputTag[StationLog]) extends ProcessFunction[StationLog, StationLog] {
  override def processElement(value: StationLog, ctx: ProcessFunction[StationLog, StationLog]#Context, out: Collector[StationLog]): Unit = {
    if (value.callType.equals("fail")) {
      ctx.output(tag, value)
    }
    out.collect(value)
  }
}