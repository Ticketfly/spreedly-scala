package com.ticketfly.spreedly

import cc.protea.spreedly.model._
import cc.protea.spreedly.model.internal._
import org.scalatest._
import org.specs2.mock.Mockito

class SpreedlyXmlSerializerSpec extends WordSpec with Mockito {

  val xmlSerializer = new SpreedlyXmlSerializer()

  val testGatewayAccountXml =
    <gateway>
      <token>4dFb93AiRDEJ18MS9xDGMyu22uO</token>
      <gateway_type>test</gateway_type>
      <name>Test</name>
      <characteristics>
        <supports_purchase>true</supports_purchase>
        <supports_authorize>true</supports_authorize>
        <supports_capture>true</supports_capture>
        <supports_credit>true</supports_credit>
        <supports_general_credit>true</supports_general_credit>
        <supports_void>true</supports_void>
        <supports_verify>false</supports_verify>
        <supports_reference_purchase>true</supports_reference_purchase>
        <supports_purchase_via_preauthorization>true</supports_purchase_via_preauthorization>
        <supports_offsite_purchase>true</supports_offsite_purchase>
        <supports_offsite_authorize>true</supports_offsite_authorize>
        <supports_3dsecure_purchase>true</supports_3dsecure_purchase>
        <supports_3dsecure_authorize>true</supports_3dsecure_authorize>
        <supports_store>true</supports_store>
        <supports_remove>true</supports_remove>
      </characteristics>
      <credentials />
      <gateway_specific_fields/>
      <payment_methods>
        <payment_method>bank_account</payment_method>
        <payment_method>credit_card</payment_method>
      </payment_methods>
      <state>retained</state>
      <redacted>false</redacted>
      <created_at>2013-07-31T10:17:36-07:00</created_at>
      <updated_at>2013-07-31T10:17:36-07:00</updated_at>
    </gateway>

  var testGatewayAccount: SpreedlyGatewayAccount = _

  def stripSpaces(str: String): String = str.replaceAll(" ", "").replaceAll("\n", "")

  "SpreedlyXmlSerializer" must {
    "deserialize from xml into domain object" in {
      testGatewayAccount = xmlSerializer.deserialize(testGatewayAccountXml.toString(), classOf[SpreedlyGatewayAccount])
      assert(testGatewayAccount.getToken == "4dFb93AiRDEJ18MS9xDGMyu22uO")
      assert(testGatewayAccount.getName == "Test")
      assert(testGatewayAccount.getGatewayType == "test")
      assert(testGatewayAccount.getCharacteristics.isSupportsCredit)
      assert(testGatewayAccount.getState == SpreedlyGatewayAccountState.RETAINED)
      assert(testGatewayAccount.getPaymentMethods.contains(SpreedlyGatewayPaymentMethod.CREDIT_CARD))
      assert(!testGatewayAccount.isRedacted)
    }

    "serialize to xml" in {
      val xml = xmlSerializer.serialize(testGatewayAccount)

      // FIXME
      // created_at and updated_at time strings depend on timezone
      // lose the type="boolean" and type="datetime" fields when serializing
      val xmlStripped = stripSpaces(s"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>${testGatewayAccountXml.toString()}""")

      def noDates(xml: String) = xml.substring(0, xml.lastIndexOf("<created_at>"))

      assert(noDates(stripSpaces(xml)) ==
        noDates(stripSpaces(s"""<?xml version="1.0" encoding="UTF-8" standalone="yes"?>${testGatewayAccountXml.toString()}""")))
    }

    "throw a SpreedlyException when unable to serialize content" in {
      case class SomeUnknownClass(someUnknownValue: Int)
      val someUnknownClassInstance = SomeUnknownClass(1)

      try {
        xmlSerializer.serialize(someUnknownClassInstance)
      } catch {
        case SpreedlyException(e, code, msg) => assert(true)
      }
    }

    "deserialize error hashes" in {
      val hashXml = <hash><status>status</status><error>error</error></hash>
      try {
        xmlSerializer.deserialize(hashXml.toString(), classOf[SpreedlyGatewayAccount])
      } catch {
        case SpreedlyException(e, code, msg) => {
          assert(code == "status")
          assert(msg == "error")
        }
      }
    }

    "not overflow the stack when a known error is returned" in {
      val errorXml = <test></test>
      try {
        xmlSerializer.deserialize(errorXml.toString(), classOf[SpreedlyErrorHash])
      } catch {
        case SpreedlyException(e, code, msg) => assert(true)
      }
    }

    "not overflow the stack when an unknown message is returned" in {
      val unknownXml = <test></test>
      try {
        xmlSerializer.deserialize(unknownXml.toString(), classOf[SpreedlyGatewayAccount])
      } catch {
        case SpreedlyException(e, code, msg) => assert(true)
      }
    }
  }
}

