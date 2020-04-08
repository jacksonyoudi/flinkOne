package org.youdi.cep

import java.util

import org.apache.flink.cep.PatternSelectFunction
import org.apache.flink.cep.scala.{CEP, PatternStream}
import org.apache.flink.cep.scala.pattern.Pattern
import org.apache.flink.streaming.api.TimeCharacteristic
import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

import scala.util.Random

object Login {
  def main(args: Array[String]): Unit = {
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment

    streamEnv.setStreamTimeCharacteristic(TimeCharacteristic.EventTime)

    val source: DataStream[LoginEvent] = streamEnv.addSource(new LoginSource)

    val stream: DataStream[LoginEvent] = source.assignAscendingTimestamps(_.eventTime)

    // 20s连续登陆失败3次

    val pattern: Pattern[LoginEvent, LoginEvent] = Pattern.begin[LoginEvent]("start").where(_.eventType.equals("type_1"))
      .next("fail_2").where(_.eventType.equals("type_1"))
      .next("fail_3").where(_.eventType.equals("type_1"))
      .within(Time.seconds(20))

    val pStream: PatternStream[LoginEvent] = CEP.pattern[LoginEvent](stream.keyBy(_.userName), pattern) // 根据用户名分组
    // 选择结果并输出
    val result: DataStream[String] = pStream.select(new PatternSelectFunction[LoginEvent, String] {
      override def select(map: util.Map[String, util.List[LoginEvent]]) = {
        val keyIter: util.Iterator[String] = map.keySet().iterator()
        val e1: LoginEvent = map.get(keyIter.next()).iterator().next()
        val e2: LoginEvent = map.get(keyIter.next()).iterator().next()
        val e3: LoginEvent = map.get(keyIter.next()).iterator().next()
        "用户:" + e1.userName + "登陆时间:" + e1.eventTime + " " + e2.eventTime + " " + e3.eventTime
      }
    })

    result.print()

    streamEnv.execute()


  }
}


case class LoginEvent(id: Long, userName: String, eventType: String, eventTime: Long)


class LoginSource extends SourceFunction[LoginEvent] {
  var flag: Boolean = true


  override def run(ctx: SourceFunction.SourceContext[LoginEvent]): Unit = {

    val random: Random = new Random()
    while (flag) {
      1.to(5).map(
        i => {
          val log: LoginEvent = LoginEvent(random.nextInt(100), "username:" + random.nextInt(5), "type_" + random.nextInt(2), System.currentTimeMillis)
          log
        }
      ).foreach(ctx.collect)

      Thread.sleep(1000)

    }
  }


  override def cancel(): Unit = {
    flag = false
  }
}