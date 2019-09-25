# Scala and the Kafka Streams DSL API - the beauty and the beast?

##Abstract
We will have a look at the Scala API of the Kafka Streams DSL, explore several scenarios, look at some specific limitations, run some examples and finally try to understand who is the beauty and who the beast ;)

##Level
Introductory



## Slides
- title slide "Scala and the Kafka Streams DSL API - the beauty and the beast?"
- intro (brief!!)
- the question: who is the beauty and who the beast?
    + In a Scala meetup that should be an easy question ;)
        * Show of hands: who thinks that Scala is the beauty?
    + Scala can be a diva: Tuple22, implicit resolution, macros, stacktraces, compile time
- Kafka intro (very brief!)
    + What is Kafka: a persistent event log
    + Kafka eco system: KSQL, Schema registry, Kafka Connect, ...
    + What is cool: partitions & consumer groups
        * automatic rebalancing
- The Kafka Streams API (stack)
    1) Java client
    2) Processor API
    3) DSL API
- What is stream processing?
    + definition ("endless", 1-n transformation, etc)
    + examples of stream processing
        * akka streams 
        * and KAFKA!
    + stateful vs. stateless (foldLeft, reduce, ....)
- Stream processing in Kafka
    + in topic -> operation -> out topic
    + "atomic" operation
    + stream vs table (vs. joinedX/groupedX)
- The first DSL - Simple flow
```
...
import org.apache.kafka.streams.scala.ImplicitConversions._
import org.apache.kafka.streams.scala.Serdes.String

object SimpleStream {
    def build(builder: StreamsBuilder) =
      builder
        .stream[String, String]("topic-in")
        .mapValues((k,v) => v + v)
        .to("topic-out")
}
```
    + Explain
        * builder pattern, fluid API with "proper" typing
        * in/out type
        * serialization / deserialization
        * show resulting topology (kafka viz)
- DSL - aggregation 
```
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  def buildWith(builder: StreamsBuilder): Unit = {

    builder
      .stream[String, String](TopicIn)
      .peek( (k,v) => logger.info(s"From input stream $k -> $v"))
      .groupByKey
      .aggregate(""){ (k, v, agg) =>
         agg + v
      }
      .toStream
      .peek( (k,v) => logger.info(s"To output stream $k -> $v"))
      .to(TopicOut)
  }

```
    + Explain
        * Grouping, KTable
        * statefulness !!!
        * null !! (beasty!)

- DSL - Joining streams
```
  val joinWindowDuration = Duration.ofMinutes(2)
  
  import org.apache.kafka.streams.scala.ImplicitConversions._
  import org.apache.kafka.streams.scala.Serdes.String

  def buildWith(builder: StreamsBuilder): Unit = {

    val streamToJoin = builder.stream[String,String](TopicToJoin)

    builder
      .stream[String, String](TopicIn)
      .peek( (k,v) => logger.info(s"From input stream $k -> $v"))
      .leftJoin(streamToJoin)( { (v1,v2) => s"$v1:$v2" }, JoinWindows.of(joinWindowDuration))
      .peek( (k,v) => logger.info(s"To output stream $k -> $v"))
      .to(TopicOut)
  }
```
    + Explain
        * join using windows
        * time ... always a mystery (event time vs processing time)
        * co-partitioning ("local" consistent table)
        * GlobalKTable for non-co-partitioned (=> eventual consistent!)
        * KStream vs KTable (compaction on topic!), "duality"
            - trigger: tream/stream - both sides vs stream/table - stream only
        
    + Reference: https://kafka.apache.org/20/documentation/streams/developer-guide/dsl-api.html#id13

- Unit testing example:
```
  "A simple stream" must {
    "map a input string to an output string by duplicating the input" in {
      MockedStreams()
        .topology { builder =>
          MySimpleStream.build(builder)
        } // Scala DSL
        .inputS("topic-in", Seq("k1" -> "v1"))
        .outputS("topic-out", 2) shouldBe Seq("k1" -> "v1v1")
    }
  }
```
    + Using a slightly pimped version of https://github.com/jpzk/mockedstreams (added implicit serdes)
- Embedded testing
    + embedded kafka, setup & configuration
    + https://github.com/manub/scalatest-embedded-kafka 
    + https://github.com/embeddedkafka/embedded-kafka (Kafka 2.3.0)
- Running it!
    + Switch to console

- Other stream functions
    + filter
    + branch
    + ...

- Extensions
    + proper branch semantic using extension classes

- Serialization - Avro
    + Intro
    + Types
        * alias Types
    + the state of the "union"
        * forward/backward compatibility 
    + Example SpecificRecord
    + Example GenericRecord

## Unsorted Slides
- Visualization with KafkaViz
- Distributed Tracing
    + common correlationId and context forwarding
- More complex serialization - AVRO 
    + Example
    + Establish in the Kafka world: KSQL, cmd line tools like kafka-avro-consumer, kafkacat, ...
- Beyond the DSL - Producer and Transformer
    + Punctuator
- Tools
    + kafkacat
    + ksql

## Topologies
### Simple stream
```
Topology: Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [topic-in])
      --> KSTREAM-PEEK-0000000001
    Processor: KSTREAM-PEEK-0000000001 (stores: [])
      --> KSTREAM-MAPVALUES-0000000002
      <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-MAPVALUES-0000000002 (stores: [])
      --> KSTREAM-PEEK-0000000003
      <-- KSTREAM-PEEK-0000000001
    Processor: KSTREAM-PEEK-0000000003 (stores: [])
      --> KSTREAM-SINK-0000000004
      <-- KSTREAM-MAPVALUES-0000000002
    Sink: KSTREAM-SINK-0000000004 (topic: topic-out)
      <-- KSTREAM-PEEK-0000000003
```

### Aggregate
```
Topology: Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000000 (topics: [topic-in])
      --> KSTREAM-PEEK-0000000001
    Processor: KSTREAM-PEEK-0000000001 (stores: [])
      --> KSTREAM-AGGREGATE-0000000003
      <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-AGGREGATE-0000000003 (stores: [KSTREAM-AGGREGATE-STATE-STORE-0000000002])
      --> KTABLE-TOSTREAM-0000000004
      <-- KSTREAM-PEEK-0000000001
    Processor: KTABLE-TOSTREAM-0000000004 (stores: [])
      --> KSTREAM-PEEK-0000000005
      <-- KSTREAM-AGGREGATE-0000000003
    Processor: KSTREAM-PEEK-0000000005 (stores: [])
      --> KSTREAM-SINK-0000000006
      <-- KTABLE-TOSTREAM-0000000004
    Sink: KSTREAM-SINK-0000000006 (topic: topic-out)
      <-- KSTREAM-PEEK-0000000005
```

### Left Join stream
```
Topology: Topologies:
   Sub-topology: 0
    Source: KSTREAM-SOURCE-0000000001 (topics: [topic-in])
      --> KSTREAM-PEEK-0000000002
    Processor: KSTREAM-PEEK-0000000002 (stores: [])
      --> KSTREAM-WINDOWED-0000000003
      <-- KSTREAM-SOURCE-0000000001
    Source: KSTREAM-SOURCE-0000000000 (topics: [topic-in-2])
      --> KSTREAM-WINDOWED-0000000004
    Processor: KSTREAM-WINDOWED-0000000003 (stores: [KSTREAM-JOINTHIS-0000000005-store])
      --> KSTREAM-JOINTHIS-0000000005
      <-- KSTREAM-PEEK-0000000002
    Processor: KSTREAM-WINDOWED-0000000004 (stores: [KSTREAM-OUTEROTHER-0000000006-store])
      --> KSTREAM-OUTEROTHER-0000000006
      <-- KSTREAM-SOURCE-0000000000
    Processor: KSTREAM-JOINTHIS-0000000005 (stores: [KSTREAM-OUTEROTHER-0000000006-store])
      --> KSTREAM-MERGE-0000000007
      <-- KSTREAM-WINDOWED-0000000003
    Processor: KSTREAM-OUTEROTHER-0000000006 (stores: [KSTREAM-JOINTHIS-0000000005-store])
      --> KSTREAM-MERGE-0000000007
      <-- KSTREAM-WINDOWED-0000000004
    Processor: KSTREAM-MERGE-0000000007 (stores: [])
      --> KSTREAM-PEEK-0000000008
      <-- KSTREAM-JOINTHIS-0000000005, KSTREAM-OUTEROTHER-0000000006
    Processor: KSTREAM-PEEK-0000000008 (stores: [])
      --> KSTREAM-SINK-0000000009
      <-- KSTREAM-MERGE-0000000007
    Sink: KSTREAM-SINK-0000000009 (topic: topic-out)
      <-- KSTREAM-PEEK-0000000008
```



