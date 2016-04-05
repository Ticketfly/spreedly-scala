package com.ticketfly.spreedly

import java.io.IOException
import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.ticketfly.spreedly.fixtures.TestCredentials
import com.ticketfly.spreedly.util.BasicHttpRequest
import com.typesafe.config.ConfigFactory
import org.scalatest._
import org.specs2.mock.Mockito
import spray.http._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import scala.reflect.ClassTag

class SpreedlyRestDispatcherSpec extends TestKit(ActorSystem("SpreedlyRestDispatcherSpec", ConfigFactory.load()))
  with WordSpecLike with BeforeAndAfter with BeforeAndAfterAll with Mockito {
  import scala.concurrent.ExecutionContext.Implicits.global

  val timeout = Duration(10, TimeUnit.SECONDS)

  val testConfig = SpreedlyConfiguration(
    environmentKey = TestCredentials.environmentKey,
    accessSecret = TestCredentials.accessSecret,
    apiUrl = "http://localhost:9090/spreedlyApi",
    requestTimeout = 10000
  )

  /**
   * Simulate the serialiation/deserialization
   */
  val mockSerializer = mock[SpreedlyXmlSerializer]
  mockSerializer.serialize(TestRequest) returns "<test>request</test>"
  mockSerializer.deserialize[TestResponse]("<test>response</test>") returns TestResponse("response")

  case object TestRequest
  case class TestResponse(text: String)

  class MockDispatcher extends SpreedlyRestDispatcher(testConfig) {
    override protected val spreedlySerializer = mockSerializer
    override protected def execute[T <: AnyRef : ClassTag](httpRequest: BasicHttpRequest): Future[T] = {
      Future.successful(TestResponse("response").asInstanceOf[T])
    }

    def buildMockHttpRequest(urlPart: String,
                             method: HttpMethod = HttpMethods.GET,
                             content: Option[AnyRef] = None,
                             queryParams: Map[String, String] = Map.empty[String, String]): BasicHttpRequest = {
      buildHttpRequest(urlPart, method, content, queryParams)
    }

  }

  val mockDispatcher = new MockDispatcher

  class MockExplodingDispatcher extends SpreedlyRestDispatcher(testConfig) {
    override protected val spreedlySerializer = mockSerializer
    override def get[T <: AnyRef : ClassTag](url: String,
                                  queryParams: Map[String, String] = Map.empty[String, String]): Future[T] = {

      val mockHttpRequest = mock[BasicHttpRequest]
      mockHttpRequest.execute(true) returns Future { throw new IOException("boom!") }
      execute(mockHttpRequest)
    }
  }

  val mockExplodingDispatcher = new MockExplodingDispatcher

  "SpreedlyRestDispatcher" must {
    "pass and encode a url" in {
      assert(mockDispatcher.buildMockHttpRequest("someUrl").getUrl == "http://localhost:9090/spreedlyApi/someUrl.xml")
      assert(mockDispatcher.buildMockHttpRequest("some url").getUrl == "http://localhost:9090/spreedlyApi/some+url.xml")
    }

    "pass the appropriate headers" in {
      val builtRequest = mockDispatcher.buildMockHttpRequest("someUrl")
      assert(builtRequest.headers.contains(HttpHeaders.Accept(MediaTypes.`application/xml`)))
      assert(builtRequest.headers.contains(
        HttpHeaders.Authorization(BasicHttpCredentials(testConfig.environmentKey, testConfig.accessSecret))
      ))
      assert(builtRequest.contentType == ContentType(MediaTypes.`application/xml`))
    }

    "pass and encode a query string" in {
      val testQueryParams = Map("testKey" -> "testVal")
      val builtRequest = mockDispatcher.buildMockHttpRequest("someUrl", HttpMethods.GET, None, testQueryParams)
      assert(builtRequest.getUrl == "http://localhost:9090/spreedlyApi/someUrl.xml?testKey=testVal")
    }

    "execute a GET request" in {
      val result = Await.result(mockDispatcher.get[TestResponse]("", Map("testKey" -> "testVal")), timeout)
      assert(result.isInstanceOf[TestResponse])
      assert(result.text == "response")
    }

    "execute a POST request" in {
      val result = Await.result(mockDispatcher.post[TestResponse](""), timeout)
      assert(result.isInstanceOf[TestResponse])
      assert(result.text == "response")
    }

    "execute a PUT request" in {
      val result = Await.result(mockDispatcher.put[TestResponse](""), timeout)
      assert(result.isInstanceOf[TestResponse])
      assert(result.text == "response")
    }

    "execute a DELETE request" in {
      val result = Await.result(mockDispatcher.delete[TestResponse](""), timeout)
      assert(result.isInstanceOf[TestResponse])
      assert(result.text == "response")
    }

    "wrap IO exceptions into SpreedlyExceptions" in {
      try {
        Await.result(mockExplodingDispatcher.get[TestResponse](""), timeout)
      } catch {
        case SpreedlyException(e, code, msg) => assert(true)
      }
    }
  }

}
