package ch.seibertec.demo.scalakafka

import net.manub.embeddedkafka.EmbeddedKafka
import net.manub.embeddedkafka.streams.EmbeddedKafkaStreamsAllInOne
import org.apache.kafka.streams.scala
import org.scalatest.{BeforeAndAfterAll, Matchers, WordSpec}

class SimpleLeftJoinStreamEmbeddedTest extends WordSpec
  with EmbeddedKafkaStreamsAllInOne
  with Matchers
  with BeforeAndAfterAll {

  import net.manub.embeddedkafka.Codecs.{stringKeyValueCrDecoder, _}
  import net.manub.embeddedkafka.ConsumerExtensions._

  "The Kafka Streams Tracing Instrumentation" should {
    "create a Producer/Stream Span when publish and read from the stream" in {
      val streamBuilder = new scala.StreamsBuilder
      SimpleLeftJoinStream.buildWith(streamBuilder)

      runStreams(Seq(SimpleLeftJoinStream.TopicIn, SimpleLeftJoinStream.TopicOut), streamBuilder.build()) {
        publishToKafka(SimpleLeftJoinStream.TopicToJoin, "hello", "Joined!")
        publishToKafka(SimpleLeftJoinStream.TopicIn, "hello", "world!")
        withConsumer[String, String, Unit] { consumer =>
          val consumedMessages: Stream[(String, String)] = consumer.consumeLazily(SimpleLeftJoinStream.TopicOut)
          consumedMessages.head shouldBe "hello" -> "world!:Joined!"
        }
      }

    }
  }

  override protected def afterAll(): Unit =
    EmbeddedKafka.stop()

}