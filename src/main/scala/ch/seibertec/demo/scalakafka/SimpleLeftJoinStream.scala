package ch.seibertec.demo.scalakafka

import java.time.Duration

import org.apache.kafka.streams.kstream.JoinWindows
import org.apache.kafka.streams.scala.StreamsBuilder
import org.slf4j.LoggerFactory

object SimpleLeftJoinStream {

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  val logger = LoggerFactory.getLogger(getClass)

  val TopicIn = "topic-in"
  val TopicToJoin = "topic-in-2"
  val TopicOut = "topic-out"

  private val joinWindowDuration = Duration.ofMinutes(2)

  def buildWith(builder: StreamsBuilder): Unit = {

    val streamToJoin = builder.stream[String,String](TopicToJoin)

    builder
      .stream[String, String](TopicIn)
      .peek( (k,v) => logger.info(s"From input stream $k -> $v"))
      .leftJoin(streamToJoin)( { (v1,v2) => s"$v1:$v2" }, JoinWindows.of(joinWindowDuration))
      .peek( (k,v) => logger.info(s"To output stream $k -> $v"))
      .to(TopicOut)
  }
}


