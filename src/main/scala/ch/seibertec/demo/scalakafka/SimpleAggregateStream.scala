package ch.seibertec.demo.scalakafka

import org.apache.kafka.streams.scala.StreamsBuilder
import org.slf4j.LoggerFactory

object SimpleAggregateStream {

  val logger = LoggerFactory.getLogger(getClass)

  val TopicIn = "topic-in"
  val TopicOut = "topic-out"

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  def buildWith(builder: StreamsBuilder): Unit =
    builder
      .stream[String, String](TopicIn)
      .peek((k, v) => logger.info(s"From input stream $k -> $v"))
      .groupByKey
      .aggregate("") { (k, v, agg) =>
        agg + v
      }
      .toStream
      .peek((k, v) => logger.info(s"To output stream $k -> $v"))
      .to(TopicOut)
}

