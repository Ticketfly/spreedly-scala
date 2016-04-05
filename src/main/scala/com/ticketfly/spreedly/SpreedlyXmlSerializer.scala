package com.ticketfly.spreedly

import javax.xml.bind.JAXBException

import cc.protea.spreedly.model.internal.SpreedlyErrorHash
import com.ticketfly.spreedly.errors.SpreedlyErrors
import com.ticketfly.spreedly.util.JAXBSerializer
import org.slf4j.LoggerFactory

import scala.reflect.ClassTag

class SpreedlyXmlSerializer extends JAXBSerializer {

  private val log = LoggerFactory.getLogger(this.getClass)

  @throws[SpreedlyException]
  override def deserialize[T <: AnyRef : ClassTag](content: String): T = {
    // In the case of a transcript, do not try to deserialize the raw string returned
    val responseType = implicitly[ClassTag[T]].runtimeClass

    if (responseType == classOf[String]) {
      return content.asInstanceOf[T]
    }

    try {
      super.deserialize(content)
    } catch {
      case e: JAXBException => {
        //log.error(s"Errors deserializing xml content ${content.toString}: ${e.getMessage}")

        // Stop trying to deserialize errors in order to prevent stack overflow
        if (responseType == classOf[SpreedlyErrors] ||  responseType == classOf[SpreedlyErrorHash]) {
          throw new SpreedlyException(e)
        }

        content match {
          case xml if xml.contains("<errors>".toCharArray) => {
            val spErrors = deserialize[SpreedlyErrors](xml)
            log.error(s"Deserialized errors as ${spErrors.errors.get(0).key}")
            throw new SpreedlyException(e, spErrors.errors.get(0).key, spErrors.errors.get(0).error)
          }

          case xml if xml.contains("<hash>".toCharArray) => {
            val hash: SpreedlyErrorHash = deserialize[SpreedlyErrorHash](xml)
            log.error(s"Deserialized hash error as ${hash.status} ${hash.error}")
            throw new SpreedlyException(e, hash.status, hash.error)
          }

          case _ => throw new SpreedlyException(e)
        }
      }
    }
  }

  @throws[SpreedlyException]
  override def serialize[T <: AnyRef](content: T): String = {
    try {
      super.serialize(content)
    } catch {
      case e: JAXBException => {
        log.error(s"Error serializing content ${content.toString}: ${e.getMessage}")
        throw new SpreedlyException(e)
      }
    }
  }
}
