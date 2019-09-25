package ch.seibertec.demo.scalakafka

import com.madewithtea.mockedstreams.MockedStreams
import org.scalatest.{Matchers, WordSpec}

class SimpleAggregateWithNullStreamUnitTest extends WordSpec with Matchers {

  import org.apache.kafka.streams.scala.Serdes.String

  "A simple stream" must {
    "map a input string to an output string by duplicating the input" in {
      MockedStreams()
        .topology { builder =>
          SimpleAggregateWithNullStream.buildWith(builder)
//          println(s"Topology: ${builder.build().describe()}")
        }
        .inputS("topic-in", Seq("k1" -> "v1", "k1" -> "skip", "k1" -> "v2"))
        .outputS("topic-out", 3) shouldBe Seq("k1" -> "v1", "k1" -> "v1v2")
    }
  }

}
