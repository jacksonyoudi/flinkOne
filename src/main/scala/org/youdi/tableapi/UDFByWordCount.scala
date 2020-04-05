package org.youdi.tableapi

import org.apache.flink.api.common.typeinfo.{TypeInformation, Types}
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.table.api.{EnvironmentSettings, Table}
import org.apache.flink.table.api.scala._
import org.apache.flink.table.functions.TableFunction
import org.apache.flink.types.Row


object UDFByWordCount {
  def main(args: Array[String]): Unit = {
    // 初始化流计算环境
    val streamEnv: StreamExecutionEnvironment = StreamExecutionEnvironment.getExecutionEnvironment
    val settings: EnvironmentSettings = EnvironmentSettings.newInstance().useOldPlanner().inStreamingMode().inStreamingMode().build()
    val tableEnv: StreamTableEnvironment = StreamTableEnvironment.create(streamEnv, settings)


    // 2. 导入隐式转换
    import org.apache.flink.streaming.api.scala._

    // 读取数据，读取socket数据流
    val stream: DataStream[String] = streamEnv.socketTextStream("127.0.0.1", 9999) // 等同spark中的 Dstream

    val table: Table = tableEnv.fromDataStream(stream, 'line)

    val myfunction: MyFlatMapFunction = new MyFlatMapFunction
    val result: Table = table.flatMap(myfunction('line)).as('word, 'cnt)
      .groupBy('word)
      .select('word, 'cnt.sum as('tot))
    val ds: DataStream[(Boolean, Row)] = tableEnv.toRetractStream[Row](result)
    ds.print()

    // 启动流计算程序
    streamEnv.execute("stream wordcount")
  }

  // 自定义一个函数类
  class MyFlatMapFunction extends TableFunction[Row] {
    override def getResultType: TypeInformation[Row] = {
      Types.ROW(Types.STRING, Types.INT)
    }

    // 函数主体
    def eval(str: String): Unit = {
      str.trim.split(" ").foreach(
        word => {
          val r: Row = new Row(2)
          r.setField(0, word)
          r.setField(1, 1)
          collect(r)
        }
      )
    }
  }

}
