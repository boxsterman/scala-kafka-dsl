package ch.seibertec.demo.scalakafka

import java.time.Instant

import org.apache.avro.generic.{GenericData, GenericRecord, GenericRecordBuilder}
import shapeless.{:+:, CNil, Coproduct, Poly1}
import org.scalatest.WordSpec

object TypeMagic {
  type MyCoProduct = String :+: BigDecimal :+: Seq[String] :+: CNil

  private def buildRecord(x: Object): GenericData.Record = {
    val builder = new GenericRecordBuilder(MyGenericUnion.SCHEMA$)
    builder.set("anCoProduct", x)
    builder.build()
  }

  object TypeMapper extends Poly1 {
    implicit def caseString = at[String](i => buildRecord(i))
    implicit def caseLong = at[BigDecimal](s => buildRecord(s))
    implicit def caseSeqString = at[Seq[String]](msr => buildRecord(msr))
  }

  def toGenericRecord(t: MyGenericUnion): GenericRecord = {
    ???
//    (t.anCoProduct map TypeMapper).select[GenericRecord].get
  }
}

class GenericMappingTest extends WordSpec {

  import TypeMagic._

  "An Avro generic type" ignore {
    "play along with shapeless" in {
      MyGenericUnion(Coproduct[MyCoProduct]("AAA"))
      MyGenericUnion(Coproduct[MyCoProduct](BigDecimal(2)))
      val x = MyGenericUnion(Coproduct[MyCoProduct](Seq("A", "B")))
      println(toGenericRecord(x))
    }
    "map specific" in {
      val x = MySpecificData(Instant.now, "xxx", 3L, None)
    }
  }
}

