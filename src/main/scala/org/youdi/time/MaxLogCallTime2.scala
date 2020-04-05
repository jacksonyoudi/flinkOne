package org.youdi.time

import org.apache.flink.api.common.functions.ReduceFunction
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.AssignerWithPeriodicWatermarks
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.watermark.Watermark
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.youdi.source.{MyCuseterSource, StationLog}


/**
 * 每隔5秒统计一下最近10s内，每个基站中通话
 */
object MaxLogCallTime2 {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 设置事件时间
    streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //周期性引入watermark的设置， 默认就是100ms
    streamEnv.getConfig.setAutoWatermarkInterval(100L)


    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    // 引入workmark 数据乱序

    // 第一种方式： 直接采assignTimestampsAndWatermarks 接口的实现类(flink实现的)
    //    stream.assignTimestampsAndWatermarks(
    //      new BoundedOutOfOrdernessTimestampExtractor[StationLog](Time.seconds(3)) {
    //        // 设置 event_time
    //        override def extractTimestamp(element: StationLog) = {
    //          element.callTime
    //        }
    //      }
    //    )

    // 第二种，自己定义一个 AssignerWithPeriodicWatermarks

    val result: DataStream[String] = stream.assignTimestampsAndWatermarks(new AssignerWithPeriodicWatermarks[StationLog] {
      var maxEventTime: Long = _

      override def getCurrentWatermark = {
        new Watermark(maxEventTime - 3000L)
      }

      // 设置event_time
      override def extractTimestamp(element: StationLog, previousElementTimestamp: Long) = {
        maxEventTime = maxEventTime.max(element.callTime)
        element.callTime
      }
    })


      //    stream.assignAscendingTimestamps(_.callTime) // 参数中指定Eventtime具体值是什么
      .filter(_.callType.equals("success"))
      // 分组开窗
      .keyBy(_.sid)
      .timeWindow(Time.seconds(10), Time.seconds(5))
      .reduce(new MyReduceFunction, new MyWindowFunction)

    result.print()

    streamEnv.execute()
  }


  class MyReduceFunction extends ReduceFunction[StationLog] {
    override def reduce(value1: StationLog, value2: StationLog): StationLog = {
      if (value1.duration > value2.duration) {
        value1
      } else {
        value2
      }
    }
  }

  class MyWindowFunction extends WindowFunction[StationLog, String, String, TimeWindow] {
    override def apply(key: String, window: TimeWindow, input: Iterable[StationLog], out: Collector[String]): Unit = {
      val builder: StringBuilder = new StringBuilder
      val log: StationLog = input.iterator.next()

      builder.append("窗口范围是:").append(window.getStart).append("--").append(window.getEnd)
      builder.append("\n")
      builder.append(log.toString)

      out.collect(builder.toString)
    }
  }

}
