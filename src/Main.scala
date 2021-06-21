import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment
import org.apache.flink.streaming.api.scala._
import org.apache.flink.streaming.api.windowing.time.Time

object Main {
  val target="b"
  def main(args: Array[String]) {
    val env = StreamExecutionEnvironment.getExecutionEnvironment
    //Linux or Mac:nc -l 9999
    //Windows:nc -l -p 9999

    val text = env.socketTextStream("localhost", 9999)

   //val stream = text.flatMap {
   // _.toLowerCase.split("\\W+") filter {
   // _.contains(target)
   // }
   // }.map {
   // ("发现目标："+_)
   // }

    //上述是将输入的含b的数据流输出

    val stream=text.flatMap(_.split(""))
      .filter(_.contains(target))
      .map((_,1))
      .keyBy(0)
      .timeWindow(Time.seconds(60))  //控制求和的时间范围，每60秒会循环开始下面操作
      .sum(1)

    //上述是将输入流分割成一个个字母筛选出含有b的并计算每分钟b出现的个数通过字符对展示出来
    stream.print().setParallelism(1)
    env.execute("Window Stream WordCount")
  }
}