package org.youdi.window

import org.apache.flink.api.common.functions.AggregateFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.scala.function.WindowFunction
import org.apache.flink.streaming.api.windowing.assigners.SlidingProcessingTimeWindows
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.api.windowing.windows.TimeWindow
import org.apache.flink.util.Collector
import org.youdi.source.{MyCuseterSource, StationLog}

object AggrateFunctionbyWindowDemo {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    // 开窗
    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)


    val result: DataStream[(String, Long)] = stream.map((log) => (log.sid, 1))
      .keyBy(_._1)
      .window(SlidingProcessingTimeWindows.of(Time.seconds(5), Time.seconds(3))) // 每3秒计算一下近5秒的数据， 开窗，滑动窗口
      .aggregate(new MyAggreateFunction, new MyWindowFunction)


    result.print()
    // 启动流计算程序
    streamEnv.execute("stream  window word count")
  }
}


/**
 * 里面的add方法是来一条数据，执行一次， getResult是在窗口结束的时候执行
 */
class MyAggreateFunction extends AggregateFunction[(String, Int), Long, Long] {
  // 初始化累加器的值
  override def createAccumulator(): Long = {
    0
  }

  // 累加
  override def add(value: (String, Int), accumulator: Long): Long = {
    accumulator + value._2
  }

  override def getResult(accumulator: Long): Long = {
    accumulator
  }

  override def merge(a: Long, b: Long): Long = {
    a + b
  }
}


/**
 * windowfunction输入数据来自于aggregateFunction,在窗口结束的时候先执行AggregateFunction对象的getResult，然后执行apply方法
 */
class MyWindowFunction extends WindowFunction[Long, (String, Long), String, TimeWindow] {
  override def apply(key: String, window: TimeWindow, input: Iterable[Long], out: Collector[(String, Long)]): Unit = {
    out.collect((key, input.iterator.next()))
  }
}