package ch.seibertec.demo.scalakafka

import com.madewithtea.mockedstreams.MockedStreams
import org.scalatest.{Matchers, WordSpec}

class SimpleAggregateStreamUnitTest extends WordSpec with Matchers {

  import org.apache.kafka.streams.scala.Serdes.String

  "A simple stream" must {
    "map a input string to an output string by duplicating the input" in {
      MockedStreams()
        .topology { builder =>
          SimpleAggregateStream.buildWith(builder)
          println(s"Topology: ${builder.build().describe()}")
        }
        .inputS("topic-in", Seq("k1" -> "v1", "k2" -> "vA", "k2" -> "vB", "k1" -> "v2"))
        .outputS("topic-out", 5) shouldBe Seq("k1" -> "v1", "k2" -> "vA", "k2" -> "vAvB", "k1" -> "v1v2")
    }
  }

}
