package ch.seibertec.demo.scalakafka

import java.time.Instant

import com.madewithtea.mockedstreams.MockedStreams
import org.scalatest.{Matchers, WordSpec}

class SimpleLeftJoinStreamUnitTest extends WordSpec with Matchers {

  import org.apache.kafka.streams.scala.Serdes.String

  "A left join stream" must {

    "join a message from the input stream with a message from the 'join' stream if within the join window - topicToJoin first" in {
      MockedStreams()
        .topology { builder =>
          SimpleLeftJoinStream.buildWith(builder)
        }
        .inputWithTimeS[String, String](
          SimpleLeftJoinStream.TopicToJoin,
          Seq(
            ("k1", "v1-join", Instant.now.toEpochMilli),
          )
        )
        .inputS(SimpleLeftJoinStream.TopicIn, Seq("k1" -> "v1"))
        .outputS(SimpleLeftJoinStream.TopicOut, 2) shouldBe Seq("k1" -> "v1:v1-join")
    }

    "join a message from the input stream with a message from the 'join' stream if within the join window - topicIn first" in {
      MockedStreams()
        .topology { builder =>
          SimpleLeftJoinStream.buildWith(builder)
        }
        .inputS(SimpleLeftJoinStream.TopicIn, Seq("k1" -> "v1"))
        .inputWithTimeS[String, String](
          SimpleLeftJoinStream.TopicToJoin,
          Seq(
            ("k1", "v1-join", Instant.now.toEpochMilli),
          )
        )
        .outputS(SimpleLeftJoinStream.TopicOut, 2) shouldBe Seq("k1" -> "v1:null", "k1" -> "v1:v1-join")
    }

    "join a message from the input stream with NULL value if join message is out of the window" in {
      MockedStreams()
        .topology { builder =>
          SimpleLeftJoinStream.buildWith(builder)
          println(s"Topology: ${builder.build().describe()}")
        }
        .inputWithTimeS[String, String](
          SimpleLeftJoinStream.TopicToJoin,
          Seq(
            ("k1", "v1-join", Instant.now.plusSeconds(60 * 6).toEpochMilli),
          )
        )
        .inputS(SimpleLeftJoinStream.TopicIn, Seq("k1" -> "v1"))
        .outputS(SimpleLeftJoinStream.TopicOut, 2) shouldBe Seq("k1" -> "v1:null")
    }
  }

}
