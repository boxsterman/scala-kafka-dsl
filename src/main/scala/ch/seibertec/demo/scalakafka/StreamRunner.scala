package ch.seibertec.demo.scalakafka

import java.time.Duration

import org.apache.kafka.streams.KafkaStreams
import org.apache.kafka.streams.scala.StreamsBuilder
import org.slf4j.LoggerFactory

object StreamRunner extends App {

  val logger = LoggerFactory.getLogger(StreamRunner.getClass)

  val builder: StreamsBuilder = new StreamsBuilder
  SimpleStream.buildWith(builder)

  // Build stream topology
  val topology = builder.build()
  logger.info(s"Topology: $topology")

  // Create a configured stream using the provided stream builder and stream config
  val ks: KafkaStreams = new KafkaStreams(topology, StreamConfiguration.settings)

  // Set the shutdown hook in order to close down this stream properly
  sys.ShutdownHookThread {
    logger.info("Shutting down stream")
    ks.close(Duration.ofSeconds(10))
  }

  // Finally start the configured stream
  logger.warn("Starting MySimpleStream ...")
  ks.start()
}

