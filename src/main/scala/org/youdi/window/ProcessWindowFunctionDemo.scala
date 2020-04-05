package org.youdi.window

import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.ProcessWindowFunction
import org.apache.flink.streaming.api.windowing.assigners.TumblingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.youdi.source.{MyCuseterSource, StationLog}


object ProcessWindowFunctionDemo {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 开窗
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    val result: DataStream[(String, Long)] = stream.map((log) => (log.sid, 1))
      .keyBy(_._1)
      .window(TumblingProcessingTimeWindows.of(Time.seconds(5))) // 每3秒计算一下近5秒的数据， 开窗，滑动窗口
      .process(new ProcessWindowFunction[(String, Int), (String, Long), String, TimeWindow] {
        override def process(key: String, context: Context, elements: Iterable[(String, Int)], out: Collector[(String, Long)]): Unit = {
          println("_ window ending")
          // 注意，整个窗口的数据都保存在迭代器中
          out.collect((key, elements.size))
        }
      })


    result.print()
    // 启动流计算程序
    streamEnv.execute("stream  window word count")
  }
}
