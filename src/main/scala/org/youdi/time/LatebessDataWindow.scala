package org.youdi.time

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.timestamps.BoundedOutOfOrdernessTimestampExtractor
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.youdi.source.{MyCuseterSource, StationLog}

object LatebessDataWindow {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 设置事件时间
    streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    //周期性引入watermark的设置， 默认就是100ms
    streamEnv.getConfig.setAutoWatermarkInterval(100L)


    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)
      .assignTimestampsAndWatermarks(
        new BoundedOutOfOrdernessTimestampExtractor[StationLog](Time.seconds(2)) {
          override def extractTimestamp(element: StationLog) = {
            element.callTime
          }
        }
      )


    // 定义一个侧输出流的标签
    val latetag: OutputTag[StationLog] = new OutputTag[StationLog]("late")

    // 分组 开窗
    val result: DataStream[String] = stream.keyBy(_.sid)
      .timeWindow(Time.seconds(10), Time.seconds(5))

      // 设置迟到的数据超过2s的情况下， 交给AllowedLateness

      // 也分两种情况， 第一种： 允许数据迟到2-5秒，再次迟到触发窗口函数， 触发条件
      // 第二种:  迟到的数据在 5s 以上 输出到侧流中
      .allowedLateness(Time.seconds(5))
      .sideOutputLateData(latetag)
      .aggregate(new MyAggregateCountFunction, new OutPutResultWindowFunctin)


    result.print("主流")
    result.getSideOutput(latetag).print("late")

    streamEnv.execute()
  }


  // 增量聚合的函数
  class MyAggregateCountFunction extends AggregateFunction[StationLog, Long, Long] {
    // 初始化一个聚合值
    override def createAccumulator(): Long = {
      0
    }

    override def add(value: StationLog, accumulator: Long): Long = {
      accumulator + 1

    }

    override def getResult(accumulator: Long): Long = {
      accumulator
    }

    override def merge(a: Long, b: Long): Long = {
      a + b
    }
  }

  class OutPutResultWindowFunctin extends WindowFunction[Long, String, String, TimeWindow] {
    override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[String]): Unit = {
      val l: Long = input.iterator.next()
      val sb = new StringBuilder
      sb.append(
        "窗口:" + window.getStart + "---" + window.getEnd + "\n"
      )
      sb.append("key" + key + " count:" + l)
      out.collect(sb.toString)
    }
  }

}
