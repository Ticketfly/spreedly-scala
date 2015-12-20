package com.ticketfly.spreedly.util

import java.io.{ByteArrayInputStream, StringWriter}
import javax.xml.bind.{JAXBContext, JAXBException, Marshaller, Unmarshaller}

class JAXBSerializer extends BasicSerializer {

  @throws[JAXBException]
  def deserialize[T <: AnyRef](xml: String, responseType: Class[T]): T = {
    val context: JAXBContext = JAXBContext.newInstance(responseType)
    val unmarshaller: Unmarshaller = context.createUnmarshaller()
    val inputStream: ByteArrayInputStream = new ByteArrayInputStream(xml.getBytes)
    unmarshaller.unmarshal(inputStream).asInstanceOf[T]
  }

  @throws[JAXBException]
  def serialize[T <: AnyRef](obj: T): String = {
    if (obj == null) {
      return ""
    }

    val context: JAXBContext  = JAXBContext.newInstance(obj.getClass)
    val marshaller: Marshaller = context.createMarshaller()
    val writer: StringWriter  = new StringWriter()
    marshaller.marshal(obj, writer)
    writer.toString
  }
}
