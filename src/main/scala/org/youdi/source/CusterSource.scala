package org.youdi.source

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._

import scala.util.Random

object CusterSource {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    streamEnv.setParallelism(1)

    val stream: DataStream[StationLog] = streamEnv.addSource(new MyCuseterSource)

    stream.print()
    streamEnv.execute("stream")

  }
}


class MyCuseterSource extends SourceFunction[StationLog] {
  var flag: Boolean = true


  override def run(ctx: SourceFunction.SourceContext[StationLog]): Unit = {

    val random: Random = new Random()
    while (flag) {
      1.to(10).map(
        i => {
          val log: StationLog = StationLog("001" + random.nextInt(10), "186" + random.nextInt(5), "187" + random.nextInt(5), "success", System.currentTimeMillis() - random.nextInt(30) * 1000, random.nextInt(10))
          log
        }
      ).foreach(ctx.collect)

      Thread.sleep(4000)

    }
  }


  override def cancel(): Unit = {
    flag = false
  }
}