package org.youdi.environment

import org.apache.flink.streaming.api.scala._


object Demo {
  def main(args: Array[String]): Unit = {
    //      StreamExecutionEnvironment.createLocalEnvironment(1)
    //    StreamExecutionEnvironment.createRemoteEnvironment()
    val env: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    env.disableOperatorChaining()
  }
}
