//通过从KaFka读取数据然后再分组下载到本地文件

import java.util.{Properties, UUID}
import org.apache.flink.api.common.serialization.SimpleStringSchema
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer010

object Main{

  def main(args: Array[String]): Unit = {

    val bootstrapServers = "bigdata35.depts.bingosoft.net:29035,bigdata36.depts.bingosoft.net:29036,bigdata37.depts.bingosoft.net:29037"

    val inputTopic = "mn_buy_ticket_P&L"

    val env = StreamExecutionEnvironment.getExecutionEnvironment

    val kafkaProperties = new Properties()

    kafkaProperties.put("bootstrap.servers", bootstrapServers)
    kafkaProperties.put("group.id", UUID.randomUUID().toString)
    kafkaProperties.put("auto.offset.reset", "earliest")
    kafkaProperties.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")
    kafkaProperties.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer")

    val inputKafkaStream = env.addSource(new FlinkKafkaConsumer010[String](inputTopic, new SimpleStringSchema(), kafkaProperties))

    val result = inputKafkaStream
      .flatMap(_.split(","))  //按逗号将语句分割得到5个分块
      .filter(_.contains("destination"))   //过滤得到含有目的地的语句块
      .map((1,_, 1))  //合成得到一个三元组，其中第一个1是作为序号，第二个1是作为计数单元
      .keyBy(1) //先按照目的地名分组
      .sum(2)  //同组的相加第二个1得到此地名实时出现的次数
      .keyBy(0)   //再以第一个序号作为条件进行分组，此时所有的语句都被分到同一组别下方便进行最大值的计算
      .timeWindow(Time.seconds(10))  //每十秒进行一次统计比较
      .maxBy(2)  //再对所有组别进行值比较，求出最大值，即为到达次数最多的地名

      result.writeAsText("C:\\Users\\bjw52328\\Desktop\\love.txt")//下载到本地文件

    env.execute()

  }

}

