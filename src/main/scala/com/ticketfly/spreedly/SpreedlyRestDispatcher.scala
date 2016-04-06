package com.ticketfly.spreedly

import java.io.IOException
import java.net.URLEncoder

import akka.actor.ActorSystem
import akka.util.Timeout
import com.ticketfly.spreedly.util.{BasicHttpRequest, RestDispatcher}
import spray.http._

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.reflect.ClassTag

class SpreedlyRestDispatcher(config: SpreedlyConfiguration)
                            (implicit system: ActorSystem) extends RestDispatcher {
  import system.dispatcher

  implicit val timeout: Timeout = config.requestTimeout.seconds

  protected val spreedlySerializer = new SpreedlyXmlSerializer()

  private def encodeUrl(urlPart: String, queryParams: Map[String, String] = Map.empty[String, String]): String = {
    val encodedUrlPart = urlPart.split("/").foldLeft("")((memo, part) => s"$memo/${URLEncoder.encode(part, "UTF-8")}")

    // In the case of a transcript, do not append the format to the URL
    val baseUrl = if (urlPart.contains("transcript")) {
      s"${config.apiUrl}$encodedUrlPart"
    } else {
      s"${config.apiUrl}$encodedUrlPart.xml"
    }

    if (queryParams.isEmpty) {
      baseUrl
    } else {
      queryParams.foldLeft(s"$baseUrl?") { (memo: String, kv: (String, String)) =>
        memo + s"${URLEncoder.encode(kv._1, "UTF-8")}=${URLEncoder.encode(kv._2, "UTF-8")}&"
      }.dropRight(1)
    }
  }

  protected def buildHttpRequest(urlPart: String,
                       method: HttpMethod = HttpMethods.GET,
                       content: Option[AnyRef] = None,
                       queryParams: Map[String, String] = Map.empty[String, String]): BasicHttpRequest = {

    implicit val timeout: Timeout = config.requestTimeout.seconds

    val request = new BasicHttpRequest(encodeUrl(urlPart, queryParams), method)
    request.contentType = ContentType(MediaTypes.`application/xml`)
    request.headers = List(
      HttpHeaders.Authorization(BasicHttpCredentials(config.environmentKey, config.accessSecret)),
      HttpHeaders.Accept(MediaTypes.`application/xml`)
    )

    if (content.nonEmpty) {
      request.body = spreedlySerializer.serialize(content.orNull)
    }

    request
  }


  protected def execute[T <: AnyRef : ClassTag](httpRequest: BasicHttpRequest): Future[T] = {
    httpRequest.execute(config.ssl).map(response => {
      spreedlySerializer.deserialize(response.entity.asString)
    }) recover {
      case e: SpreedlyException => throw e
      case e: IOException => throw new SpreedlyException(e)
    }
  }

  def get[T <: AnyRef : ClassTag](url: String, queryParams: Map[String, String] = Map.empty[String, String]): Future[T] = {
    execute(buildHttpRequest(url, HttpMethods.GET, None, queryParams))
  }

  def options[T <: AnyRef : ClassTag](url: String, queryParams: Map[String, String] = Map.empty[String, String]): Future[T] = {
    execute(buildHttpRequest(url, HttpMethods.OPTIONS, None, queryParams))
  }

  def put[T <: AnyRef : ClassTag](url: String,
                       content: Option[AnyRef] = None,
                       queryParams: Map[String, String] = Map.empty[String, String]): Future[T] = {
    execute(buildHttpRequest(url, HttpMethods.PUT, content, queryParams))
  }

  def post[T <: AnyRef : ClassTag](url: String,
                        content: Option[AnyRef] = None,
                        queryParams: Map[String, String] = Map.empty[String, String]): Future[T] = {
    execute(buildHttpRequest(url, HttpMethods.POST, content, queryParams))
  }

  def delete[T <: AnyRef : ClassTag](url: String, queryParams: Map[String, String] = Map.empty[String, String]): Future[T] = {
    execute(buildHttpRequest(url, HttpMethods.DELETE, None, queryParams))
  }

}
