package ch.seibertec.demo.scalakafka

import org.apache.kafka.streams.scala.StreamsBuilder
import org.slf4j.LoggerFactory

object SimpleStream {

  val logger = LoggerFactory.getLogger(SimpleStream.getClass)

  val TopicIn = "topic-in"
  val TopicOut = "topic-out"

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  def buildWith(builder: StreamsBuilder): Unit =
    builder
      .stream[String, String](TopicIn)
      .peek( (k,v) => logger.info(s"From input stream $k -> $v"))
      .mapValues((k, v) => v + v)
      .peek( (k,v) => logger.info(s"To output stream $k -> $v"))
      .to(TopicOut)
}
