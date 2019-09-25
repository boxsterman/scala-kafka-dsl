package ch.seibertec.demo.scalakafka

import org.apache.kafka.streams.scala.StreamsBuilder
import org.slf4j.LoggerFactory

object SimpleAggregateWithNullStream {

  val logger = LoggerFactory.getLogger(getClass)

  val TopicIn = "topic-in"
  val TopicOut = "topic-out"

  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  def buildWith(builder: StreamsBuilder): Unit = {

    builder
      .stream[String, String](TopicIn)
      .peek( (k,v) => logger.info(s"From input stream $k -> $v"))
      .filter((k,v) => v != "skip")
      .groupByKey
      .aggregate(""){ (k, v, agg) =>
        if(v == "skip")
          null
        else
         agg + v
      }
      .toStream
      .peek( (k,v) => logger.info(s"To output stream $k -> $v"))
      .to(TopicOut)
  }
}


