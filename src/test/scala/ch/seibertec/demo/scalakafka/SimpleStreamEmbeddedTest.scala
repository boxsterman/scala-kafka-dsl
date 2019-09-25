package ch.seibertec.demo.scalakafka

import net.manub.embeddedkafka.EmbeddedKafka
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.apache.kafka.streams.scala
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class SimpleStreamEmbeddedTest extends WordSpec
  with EmbeddedKafkaStreamsAllInOne
  with BeforeAndAfterAll
  with Matchers {

  import net.manub.embeddedkafka.Codecs._
  import net.manub.embeddedkafka.ConsumerExtensions._
  import net.manub.embeddedkafka.Codecs.stringKeyValueCrDecoder

  "The Kafka Streams Tracing Instrumentation" should {
    "create a Producer/Stream Span when publish and read from the stream" in {
      val streamBuilder = new scala.StreamsBuilder
      SimpleStream.buildWith(streamBuilder)

      runStreams(Seq(SimpleStream.TopicIn, SimpleStream.TopicOut), streamBuilder.build()) {
        publishToKafka(SimpleStream.TopicIn, "hello", "world!")
        withConsumer[String, String, Unit] { consumer =>
          val consumedMessages: Stream[(String, String)] = consumer.consumeLazily(SimpleStream.TopicOut)
          consumedMessages.head shouldBe "hello" -> "world!world!"
        }
      }
    }
  }
  override protected def afterAll(): Unit =
    EmbeddedKafka.stop()
    Thread.sleep(2000)
}
