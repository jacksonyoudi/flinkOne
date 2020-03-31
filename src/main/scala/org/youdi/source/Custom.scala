package org.youdi.source

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._

import scala.collection.immutable
import scala.util.Random

object Custom {
  def main(args: Array[String]): Unit = {
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    val stream: DataStream[SensorReading] = env.addSource(new SensorSource)
    stream.print("custom").setParallelism(8)
    env.execute("custom")
  }
}


class SensorSource() extends SourceFunction[SensorReading] {
  //定义一个tag， 表示数据源是否正常运行
  var running: Boolean = true


  // 正常生成数据
  override def run(ctx: SourceFunction.SourceContext[SensorReading]): Unit = {
    val random: Random = new Random()


    // 初始化定义一组传感器温度数据
    val curTemp: immutable.IndexedSeq[(String, Double)] = 1.to(10).map(
      i => ("sensor_" + i, 60 + random.nextGaussian() * 20)
    )

    while (running) {
      // 在前一次温度的基础上更新温度值
      val seq: immutable.IndexedSeq[(String, Double)] = curTemp.map(
        t => (t._1, t._2 + random.nextGaussian())
      )
      val curTime: Long = System.currentTimeMillis()


      seq.foreach(
        t => ctx.collect(SensorReading(t._1, curTime, t._2))
      )
      Thread.sleep(1000)

    }


  }

  // 取消数据源的生成
  override def cancel(): Unit = {
    running = false
  }
}


