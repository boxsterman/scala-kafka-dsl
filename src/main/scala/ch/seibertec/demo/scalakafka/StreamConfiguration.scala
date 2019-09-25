package ch.seibertec.demo.scalakafka

import java.util.Properties

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.processor.WallclockTimestampExtractor

object StreamConfiguration {
  // basic configuration
  val maxPoll:             java.lang.Integer = 300
  val maxPollMs:           java.lang.Integer = 900000
  val cacheSizeInBytes:    java.lang.Long    = 0L     // disable caching
  val cacheCommitInterval: java.lang.Long    = 30000L // Default as in Kafa config

  val settings = new Properties
  settings.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, cacheSizeInBytes)
  settings.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, cacheCommitInterval)
  settings.put(StreamsConfig.APPLICATION_ID_CONFIG, "MyStreamRunner")
  settings.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092")
  settings.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPoll)
  settings.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollMs)
  settings.put(StreamsConfig.STATE_DIR_CONFIG, "./stores")
  settings.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, classOf[WallclockTimestampExtractor])
  // Other potentially interesting settings to avoid "poisonous" messages
//  settings.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG, classOf[XXX])
}
