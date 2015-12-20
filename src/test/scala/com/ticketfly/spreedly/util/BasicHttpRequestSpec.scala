package com.ticketfly.spreedly.util

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfter, BeforeAndAfterAll, WordSpecLike}
import org.specs2.mock.Mockito
import akka.util.Timeout
import spray.http._
import scala.concurrent.Await
import scala.concurrent.duration._

class BasicHttpRequestSpec extends TestKit(ActorSystem("BasicHttpRequestSpec", ConfigFactory.load()))
  with WordSpecLike with BeforeAndAfter with BeforeAndAfterAll with Mockito {

  implicit val timeout: Timeout = Timeout(10.seconds)

  class MockBasicHttpRequest(url: String) extends BasicHttpRequest(url) {

    val mockHeaders = List(
      HttpHeaders.`Content-Type`(ContentType(MediaTypes.`text/plain`)),
      HttpHeaders.`Content-Length`(4L)
    )

    def mockTracedResponse: String = {
      val response = new HttpResponse(
        status = StatusCodes.OK,
        entity = HttpEntity("test"),
        headers = mockHeaders,
        protocol = HttpProtocols.`HTTP/1.1`
      )
      traceResponse(response)
    }

    def mockTracedRequest: String = {
      headers = mockHeaders
      body = "test"
      traceRequest
    }
  }

  "BasicHttpRequest" must {
    "trace http requests and responses" in {
      val request = new BasicHttpRequest("http://google.com")

      val response = Await.result(request.execute(), 10.seconds)
      assert(response.entity.nonEmpty)
    }

    "trace http request to log" in {
      val mockBasicHttpRequest = new MockBasicHttpRequest("test")
      val response = mockBasicHttpRequest.mockTracedResponse
      assert(response.nonEmpty)
    }

    "trace http request/response to log" in {
      val mockBasicHttpRequest = new MockBasicHttpRequest("test")
      val tracedRequestStr = mockBasicHttpRequest.mockTracedRequest
      assert(tracedRequestStr.nonEmpty)

      val tracedResponseStr = mockBasicHttpRequest.mockTracedResponse
      assert(tracedResponseStr.nonEmpty)
    }

    "send a request over https" in {
      val request = new BasicHttpRequest("https://google.com")

      val response = Await.result(request.execute(sslEnabled = true), 10.seconds)
      assert(response.entity.nonEmpty)
    }
  }
}

