package com.ticketfly.spreedly.util

import akka.actor.ActorSystem
import akka.io._
import akka.pattern.ask
import akka.util.Timeout
import org.slf4j.LoggerFactory
import spray.can.Http
import spray.can.Http.HostConnectorSetup
import spray.http
import spray.http.{HttpResponse, _}
import scala.concurrent.Future

class BasicHttpRequest(url: String, method: HttpMethod = HttpMethods.GET)
                      (implicit val system: ActorSystem, timeout: Timeout) {

  import system.dispatcher

  private val logger = LoggerFactory.getLogger(this.getClass)

  var headers     = List.empty[HttpHeader]
  var contentType = ContentType(MediaTypes.`text/plain`)
  var body        = ""

  def getUrl: String = url

  protected def traceRequest: String = {
    val str = new StringBuilder()
    str.append(s"Http Request: $method $url")
    if (headers.nonEmpty) {
      str.append("\tHeaders:")
      headers.foreach((h) => str.append(s"  - ${h.name}: ${h.value}"))
    }

    if (body.trim.nonEmpty) {
      str.append(s"\t- Content-Type: $contentType")
      str.append(s"\t- Body: $body")
    }

    str.toString()
  }

  protected def traceResponse(response: HttpResponse): String = {
    val str = new StringBuilder()
    str.append(s"Http Response: Status ${response.status}")

    str.append("\tHeaders:")
    response.headers.foreach((h) => str.append(s"  - ${h.name}: ${h.value}"))

    str.append(s"\tBody: ${response.entity.asString}")

    str.toString()
  }

  def execute(sslEnabled: Boolean = false): Future[HttpResponse] = {
    logger.debug(traceRequest)
    val uri     = Uri(url)
    val entity  = HttpEntity(contentType, body)
    val request = http.HttpRequest(method, Uri(url), headers, entity)

    for {
      setup     <- IO(Http) ? HostConnectorSetup(uri.authority.host.address, uri.authority.port, sslEnabled)
      response  <- (IO(Http) ? request).mapTo[HttpResponse]
    } yield {
      logger.debug(traceResponse(response))
      response
    }
  }
}
