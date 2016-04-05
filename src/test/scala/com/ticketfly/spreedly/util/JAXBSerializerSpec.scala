package com.ticketfly.spreedly.util

import com.ticketfly.spreedly.{ThingGroup, Thing}
import org.scalatest._
import org.specs2.mock.Mockito
import scala.collection.JavaConverters._

class JAXBSerializerSpec extends WordSpec with Mockito {

  val xmlSerializer = new JAXBSerializer()

  val thing1 = new Thing()
  thing1.id = 1
  thing1.name = "one"

  val thing2 = new Thing()
  thing2.id = 2
  thing2.name = "two"

  val thing3 = new Thing()
  thing3.id = 3
  thing3.name = "three"

  val thingGroup = new ThingGroup()
  thingGroup.things = new java.util.ArrayList()
  thingGroup.things.add(thing1)
  thingGroup.things.add(thing2)
  thingGroup.things.add(thing3)


  val thingXmlString = """<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
                         |<thing_group>
                         |<thing name="one">1</thing>
                         |<thing name="two">2</thing>
                         |<thing name="three">3</thing>
                         |</thing_group>
                         |""".stripMargin.replaceAll("\n", "")

  "JAXBSerializer" must {
    "serialize to xml" in {
      val xml = xmlSerializer.serialize(thingGroup)
      assert(xml == thingXmlString)
    }

    "deserialize from xml into domain object" in {
      val group: ThingGroup = xmlSerializer.deserialize[ThingGroup](thingXmlString.toString)

      val groupThings = group.things.asScala.toSeq
      assert(groupThings.nonEmpty)

      val groupThingNames = groupThings.map(_.name)

      assert(groupThingNames.contains("one"))
      assert(groupThingNames.contains("two"))
      assert(groupThingNames.contains("three"))
    }

    "return an empty string when nothing passed to serialize" in {
      val str = xmlSerializer.serialize(null)
      assert(str == "")
    }
  }
}
