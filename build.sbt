lazy val kafkaVersion = "2.3.0"
lazy val avroVersion = "1.9.1"

/**
 * Configure Avrohugger plugin:
 * - to use different source paths for specific (src/main/avro) and generic (src/main/avro) mappings
 * - to generate Schema information in the generic case classes (required for setting/extracting data)
 * - to hook into the compile task
 */
lazy val avroSettings = Seq(
  sourceGenerators in Compile += (avroScalaGenerate in Compile).taskValue,
  sourceGenerators in Compile += (avroScalaGenerateSpecific in Compile).taskValue,
  avroSpecificSourceDirectories in Compile := Seq((sourceDirectory in Compile).value / "avro"),
  avroSourceDirectories in Compile := Seq((sourceDirectory in Compile).value / "avro-generic"),
  avroScalaCustomTypes in Compile := {
    avrohugger.format.Standard.defaultTypes.copy(
      record = avrohugger.types.ScalaCaseClassWithSchema)
  }
)

lazy val root = (project in file("."))
  .settings(
    name := "scala-kafka-dsl",
    scalaVersion := "2.12.8",
    version := "0.1",
    libraryDependencies ++= List(
      "org.apache.kafka" %% "kafka-streams-scala" % kafkaVersion,
      "org.apache.avro" % "avro" % avroVersion,                                 // for generic mappings of Avro
      "com.chuusai" %% "shapeless" % "2.3.3",                                   // for generic mappings of Avro
      "org.slf4j" % "slf4j-simple" % "1.7.26",
      "org.scalatest" %% "scalatest" % "3.0.8" % "test",
      "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion % "test",
      "io.github.embeddedkafka" %% "embedded-kafka" % kafkaVersion % "test",
      "net.manub" %% "scalatest-embedded-kafka-streams" % "2.0.0" % "test"
    ),
    concurrentRestrictions in Global += Tags.limit(Tags.Test, 1),     // to ensure that only one embedded kafka instance is running at a time
    avroSettings
  )