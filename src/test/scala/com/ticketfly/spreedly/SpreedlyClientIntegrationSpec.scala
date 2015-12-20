package com.ticketfly.spreedly

import java.util.concurrent.TimeUnit
import akka.actor.ActorSystem
import akka.testkit.TestKit
import com.ticketfly.spreedly.fixtures.{TestCreditCards, TestCredentials}
import cc.protea.spreedly.model._
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfter, WordSpecLike, WordSpec}
import org.specs2.mock.Mockito
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.collection.JavaConverters._

class SpreedlyClientIntegrationSpec extends TestKit(ActorSystem("SpreedlyClientIntegrationSpec", ConfigFactory.load()))
  with WordSpecLike with BeforeAndAfter with BeforeAndAfterAll with Mockito {

  // set to 64 seconds per the docs https://docs.spreedly.com/reference/api/v1/timeouts/
  val timeout = Duration(64, TimeUnit.SECONDS)

  val testConfig = SpreedlyConfiguration(
    environmentKey = TestCredentials.environmentKey,
    accessSecret = TestCredentials.accessSecret,
    requestTimeout = 10000
  )

  val spreedly = new SpreedlyClient(testConfig)

  private def getTestGatewayAccount: SpreedlyGatewayAccount = {
    val result = Await.result(spreedly.listGatewayAccounts(None, false, true), timeout)
    result.filter(acc => {
      acc.getGatewayType == "test" && !acc.isRedacted && acc.getState == SpreedlyGatewayAccountState.RETAINED
    }).head
  }

  private def getTestPaymentMethod(retained: Boolean = true): SpreedlyPaymentMethod = {
    val result = Await.result(spreedly.listPaymentMethods(None, false, retained), timeout)
    result.filter(pm => {
      if (retained) pm.getStorageState == SpreedlyStorageState.RETAINED && pm.isTest
      else pm.getStorageState == SpreedlyStorageState.CACHED && pm.isTest
    }).head
  }

  private def createTestPaymentMethod: SpreedlyPaymentMethod = {
    val testCreditCard: SpreedlyCreditCard = TestCreditCards.randomValid
    Await.result(spreedly.createPaymentMethod(testCreditCard), timeout).getPaymentMethod
  }

  private def createTestGatewayAccount: SpreedlyGatewayAccount = {
    val testAccount = new SpreedlyGatewayAccount()
    testAccount.setGatewayType("test")
    val authorizeChars = new SpreedlyGatewayCharacteristics()
    authorizeChars.setSupportsAuthorize(true)
    authorizeChars.setSupportsCapture(true)
    testAccount.setCharacteristics(authorizeChars)
    Await.result(spreedly.createGatewayAccount(testAccount), timeout)
  }

  private def createTestPurchase(ga: SpreedlyGatewayAccount, pm: SpreedlyPaymentMethod) = {
    Await.result(spreedly.purchase(ga.getToken, pm.getToken, 100, "USD"), timeout)
  }


  private def assertCredentialsSet(account: SpreedlyGatewayAccount) = {
    assert(account.getCredentials.asScala.nonEmpty)
    assert(account.getCredentials.asScala.size == 2)
    assert(account.getCredentials.get(0).getName == "login")
    assert(account.getCredentials.get(0).getValue == "someUsername")
    assert(account.getCredentials.get(0).isSafe)
    assert(account.getCredentials.get(1).getName == "password")
    assert(!account.getCredentials.get(1).isSafe)
  }

  private def setTestCredentials(account: SpreedlyGatewayAccount) = {
    val loginCredential = new SpreedlyGatewayCredential()
    loginCredential.setLabel("login")
    loginCredential.setName("login")
    loginCredential.setValue("someUsername")
    loginCredential.setSafe(true)

    val passwordCredential = new SpreedlyGatewayCredential()
    passwordCredential.setLabel("password")
    passwordCredential.setName("password")
    passwordCredential.setValue("somePassword")
    passwordCredential.setSafe(false)

    val credentials = List(loginCredential, passwordCredential).asJava

    account.setCredentials(credentials)
  }

  "SpreedlyClient" must {

    /**
     * Gateway Providers
     */
    "list gateway providers" in {
      val result = Await.result(spreedly.listGatewayProviders, timeout)
      assert(result.isInstanceOf[Seq[SpreedlyGatewayProvider]])
      assert(result.map(_.getName).contains("NetPay"))
    }

    "get a test gateway provider" in {
      val result = Await.result(spreedly.getGatewayProvider("Spreedly Test"), timeout)
      assert(result.isInstanceOf[Option[SpreedlyGatewayProvider]])
      assert(result.nonEmpty)
      assert(result.get.getName == "Spreedly Test")
    }


    /**
     * Gateway Accounts
     */
    "create a gateway account" in {
      val testAccount = new SpreedlyGatewayAccount()
      testAccount.setGatewayType("test")

      val result = Await.result(spreedly.createGatewayAccount(testAccount), timeout)
      assert(result.isInstanceOf[SpreedlyGatewayAccount])
      assert(result.getName == "Spreedly Test")
      assert(result.getToken.nonEmpty)
    }

    "create a gateway account with credentials" in {
      val testAccount = new SpreedlyGatewayAccount()
      testAccount.setGatewayType("test")

      setTestCredentials(testAccount)

      // First verify they are properly set in the JAXB document
      assertCredentialsSet(testAccount)
      assert(testAccount.getCredentials.get(1).getValue == "somePassword")

      val result = Await.result(spreedly.createGatewayAccount(testAccount), timeout)

      // Then verify the credentials come back properly in the response
      assertCredentialsSet(testAccount)

      assert(result.isInstanceOf[SpreedlyGatewayAccount])
      assert(result.getName == "Spreedly Test")
      assert(result.getToken.nonEmpty)
    }

    "list gateway accounts" in {
      val result = Await.result(spreedly.listGatewayAccounts(None, true), timeout)
      assert(result.isInstanceOf[Seq[SpreedlyGatewayAccount]])
      assert(result.nonEmpty)

      // List contains the gateway token we just created
      //assert(result.map(_.getToken).contains(testGatewayToken))

      // Store the test gateway token for use in later tests
      //testGatewayAccount = result.filter(_.getGatewayType == "test").head
      //testGatewayToken = testGatewayAccount.getToken
    }

    "get a gateway account" in {
      val testGatewayAccount = createTestGatewayAccount

      val result = Await.result(spreedly.getGatewayAccount(testGatewayAccount.getToken), timeout)
      assert(result.isInstanceOf[SpreedlyGatewayAccount])
      assert(result.getName == "Spreedly Test")
    }

    "fail to get a gateway account that doesn't exist" in {
      val badAccountToken = "someToken"
      try {
        Await.result(spreedly.getGatewayAccount(badAccountToken), timeout)
      } catch {
        case SpreedlyException(e, code, msg) => {
          assert(code == "errors.gateway_not_found")
          assert(msg == "Unable to find the specified gateway.")
        }
      }
    }

    "redact a gateway account" in {
      val testGatewayAccount = createTestGatewayAccount

      val result = Await.result(spreedly.redactGatewayAccount(testGatewayAccount), timeout)
      val gatewayResult = Await.result(spreedly.getGatewayAccount(testGatewayAccount.getToken), timeout)
      assert(gatewayResult.getState == SpreedlyGatewayAccountState.REDACTED)
      assert(gatewayResult.isRedacted)
    }


    "update a gateway account's credentials" in {
      val testGatewayAccount = createTestGatewayAccount

      setTestCredentials(testGatewayAccount)

      // First verify they are properly set in the JAXB document
      assertCredentialsSet(testGatewayAccount)
      assert(testGatewayAccount.getCredentials.get(1).getValue == "somePassword")


      val result = Await.result(spreedly.updateGatewayAccount(testGatewayAccount), timeout)
      assert(result.isInstanceOf[SpreedlyGatewayAccount])

      // Then verify the credentials come back properly in the response
      assertCredentialsSet(testGatewayAccount)

      // Verify it is the same gateway
      assert(result.getName == "Spreedly Test")
      assert(result.getToken == testGatewayAccount.getToken)

      // Updating will also set to retained status
      assert(result.getState == SpreedlyGatewayAccountState.RETAINED)
    }

    "update a gateway account's credentials when no gateway type is included" in {
      val testGatewayAccount = createTestGatewayAccount
      testGatewayAccount.setGatewayType(null)

      setTestCredentials(testGatewayAccount)

      // First verify they are properly set in the JAXB document
      assertCredentialsSet(testGatewayAccount)
      assert(testGatewayAccount.getCredentials.get(1).getValue == "somePassword")


      val result = Await.result(spreedly.updateGatewayAccount(testGatewayAccount), timeout)
      assert(result.isInstanceOf[SpreedlyGatewayAccount])

      // Then verify the credentials come back properly in the response
      assertCredentialsSet(testGatewayAccount)

      // Verify it is the same gateway
      assert(result.getName == "Spreedly Test")
      assert(result.getToken == testGatewayAccount.getToken)

      // Updating will also set to retained status
      assert(result.getState == SpreedlyGatewayAccountState.RETAINED)
    }

    "retain a gateway account" in {
      val testGatewayAccount = createTestGatewayAccount

      val result = Await.result(spreedly.retainGatewayAccount(testGatewayAccount), timeout)
      assert(result.succeeded)

      // Double check against the gateway's state info
      val updatedGateway = Await.result(spreedly.getGatewayAccount(testGatewayAccount.getToken), timeout)
      assert(updatedGateway.getState == SpreedlyGatewayAccountState.RETAINED)
    }


    /**
     * Payment Methods
     */
    "create a payment method" in {
      val testCreditCard: SpreedlyCreditCard = TestCreditCards.randomValid

      val result = Await.result(spreedly.createPaymentMethod(testCreditCard), timeout)
      assert(result.getToken.nonEmpty)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.getPaymentMethod.getPaymentMethodType == SpreedlyPaymentMethodType.CREDIT_CARD)
      assert(result.getPaymentMethod.getStorageState == SpreedlyStorageState.CACHED)
    }

    "fail to create a payment for a card without the card number" in {
      val card = new SpreedlyCreditCard()
      card.setCardType(SpreedlyCardType.VISA)
      card.setEmail("testEmail@test.com")
      card.setData("testCardData")
      card.setMonth(11)
      card.setYear(2018)
      card.setFirstName("Joe")
      card.setLastName("Johnson")
      card.setVerificationValue("123")

      try {
        Await.result(spreedly.createPaymentMethod(card), timeout)
      } catch {
        case SpreedlyException(e, code, msg) => {
          assert(code == "errors.account_inactive")
        }
      }
    }


    "get a payment method" in {
      val testPaymentMethod = createTestPaymentMethod

      val result = Await.result(spreedly.getPaymentMethod(testPaymentMethod.getToken), timeout)
      assert(result.isInstanceOf[SpreedlyPaymentMethod])
      assert(result.getPaymentMethodType == SpreedlyPaymentMethodType.CREDIT_CARD)
      assert(result.getToken == testPaymentMethod.getToken)
    }


    "retain a payment method" in {
      val testPaymentMethod = createTestPaymentMethod

      val result = Await.result(spreedly.retainPaymentMethod(testPaymentMethod.getToken), timeout)
      assert(result.succeeded)

      assert(result.getPaymentMethod.getStorageState == SpreedlyStorageState.RETAINED)
    }

    "recache a payment method" in {
      val testPaymentMethod = createTestPaymentMethod
      val testPaymentMethodToken = testPaymentMethod.getToken
      val testPaymentMethodVerificationValue = testPaymentMethod.getVerificationValue

      val result = Await.result(spreedly.recachePaymentMethod(testPaymentMethodToken, testPaymentMethodVerificationValue), timeout)
      assert(result.getPaymentMethod.getStorageState == SpreedlyStorageState.CACHED)
    }

    "list payment methods" in {
      val testPaymentMethod = createTestPaymentMethod

      // And retain it
      val retainResult = Await.result(spreedly.retainPaymentMethod(testPaymentMethod.getToken), timeout)
      assert(retainResult.succeeded)
      assert(retainResult.getPaymentMethod.getStorageState == SpreedlyStorageState.RETAINED)

      val result = Await.result(spreedly.listPaymentMethods(None, true, true), timeout)
      assert(result.isInstanceOf[Seq[SpreedlyPaymentMethod]])
      assert(result.nonEmpty)

      //val resultSince = Await.result(spreedly.listPaymentMethods(Some(testPaymentMethod.getToken), true), timeout)
      //assert(resultSince.isInstanceOf[Seq[SpreedlyPaymentMethod]])
      //assert(resultSince.nonEmpty)
      //assert(result.map(_.getToken).contains(testPaymentMethodToken))
    }

    "verify a gateway account and retain on success" in {
      val testGatewayAccount = createTestGatewayAccount
      val testPaymentMethod = createTestPaymentMethod

      val request = new SpreedlyTransactionRequest()
      request.setGatewayAccountToken(testGatewayAccount.getToken)
      request.setAmountInCents(100)
      request.setCurrencyCode("USD")
      request.setPaymentMethodToken(testPaymentMethod.getToken)
      request.setRetainOnSuccess(true)

      val result = Await.result(spreedly.verifyGatewayAccount(request), timeout)
      assert(result.getToken.nonEmpty)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
    }

    "update a payment method" in {
      val createdPaymentMethod = createTestPaymentMethod
      val createdPaymentMethodToken = createdPaymentMethod.getToken

      val updatePaymentMethod = new SpreedlyPaymentMethod()
      updatePaymentMethod.setToken(createdPaymentMethodToken)
      updatePaymentMethod.setFirstName("Dog")
      updatePaymentMethod.setAddress1("new address")

      val result = Await.result(spreedly.updatePaymentMethod(updatePaymentMethod), timeout)
      assert(result.isInstanceOf[SpreedlyPaymentMethod])
      assert(result.getToken == updatePaymentMethod.getToken)
      assert(result.getAddress1 == "new address")
      assert(result.getFirstName == "Dog")
    }

    "purchase through gateway and payment method" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount

      val result = Await.result(spreedly.purchase(testGatewayAccount.getToken, testPaymentMethod.getToken, 100, "USD"), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.isRetainOnSuccess)
    }

    "purchase through gateway and payment method and retain on success" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount

      val request = new SpreedlyTransactionRequest()
      request.setAmountInCents(100)
      request.setCurrencyCode("USD")
      request.setGatewayAccountToken(testGatewayAccount.getToken)
      request.setPaymentMethodToken(testPaymentMethod.getToken)
      request.setRetainOnSuccess(true)

      val result = Await.result(spreedly.purchase(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getPaymentMethod.isTest)
    }

    "redact a payment method with an associated gateway" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount

      val result = Await.result(spreedly.redactPaymentMethod(testPaymentMethod.getToken, Some(testGatewayAccount.getToken)), timeout)
      assert(result.succeeded)
    }

    "redact a payment method without an associated gateway" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount

      val result = Await.result(spreedly.redactPaymentMethod(testPaymentMethod.getToken), timeout)
      assert(result.succeeded)
    }


    "fail to purchase through a redacted payment method" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount

      val redactResult = Await.result(spreedly.redactPaymentMethod(testPaymentMethod.getToken, Some(testGatewayAccount.getToken)), timeout)
      assert(redactResult.succeeded)

      val request = new SpreedlyTransactionRequest()
      request.setAmountInCents(100)
      request.setCurrencyCode("USD")
      request.setGatewayAccountToken(testGatewayAccount.getToken)
      request.setPaymentMethodToken(testPaymentMethod.getToken)

      try {
        Await.result(spreedly.purchase(request), timeout)
      } catch {
        case SpreedlyException(e, code, msg) => assert(true)
      }
    }

    /**
     * Transaction Methods
     */
    "get a transaction by token" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val result = Await.result(spreedly.getTransaction(testPurchase.getToken), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
    }

    "fail to get an invalid transaction token" in {
      val badToken = "someBadToken"
      try {
        Await.result(spreedly.getTransaction(badToken), timeout)
      } catch {
        case SpreedlyException(e, code, msg) => {
          assert(code == "errors.transaction_not_found")
          assert(msg == "Unable to find the transaction someBadToken.")
        }
      }
    }

    "void a transaction" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val request = new SpreedlyTransactionRequest()
      request.setReferenceTransactionToken(testPurchase.getToken)

      val result = Await.result(spreedly.voidTransaction(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
    }

    "list gateway account transactions" in {
      val testGatewayAccount = createTestGatewayAccount
      val testPaymentMethod1 = createTestPaymentMethod
      val testPaymentMethod2 = createTestPaymentMethod
      val testPurchase1 = createTestPurchase(testGatewayAccount, testPaymentMethod1)
      val testPurchase2 = createTestPurchase(testGatewayAccount, testPaymentMethod2)
      val testPurchase3 = createTestPurchase(testGatewayAccount, testPaymentMethod1)

      val result = Await.result(spreedly.listGatewayTransactions(testGatewayAccount.getToken, None, true), timeout)
      assert(result.isInstanceOf[Seq[SpreedlyTransactionResponse]])
      assert(result.nonEmpty)
      assert(result.map(_.getGatewayToken).contains(testGatewayAccount.getToken))
      assert(result.map(_.getToken).contains(testPurchase1.getToken))
      assert(result.map(_.getToken).contains(testPurchase2.getToken))
      assert(result.map(_.getToken).contains(testPurchase3.getToken))

      val resultSince = Await.result(spreedly.listGatewayTransactions(testGatewayAccount.getToken, Some(testPurchase1.getToken)), timeout)
      assert(result.isInstanceOf[Seq[SpreedlyTransactionResponse]])
      assert(result.nonEmpty)
    }

    "list payment method transactions" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val result = Await.result(spreedly.listPaymentMethodTransactions(testPaymentMethod.getToken, None, true), timeout)
      assert(result.isInstanceOf[Seq[SpreedlyTransactionResponse]])
      assert(result.nonEmpty)
      assert(result.map(_.getGatewayToken).contains(testGatewayAccount.getToken))
      assert(result.map(_.getToken).contains(testPurchase.getToken))
    }


    "list all transactions" in {
      val testGatewayAccount1 = createTestGatewayAccount
      val testGatewayAccount2 = createTestGatewayAccount
      val testPaymentMethod1 = createTestPaymentMethod
      val testPaymentMethod2 = createTestPaymentMethod
      val testPurchase1 = createTestPurchase(testGatewayAccount1, testPaymentMethod1)
      val testPurchase2 = createTestPurchase(testGatewayAccount2, testPaymentMethod2)
      val testPurchase3 = createTestPurchase(testGatewayAccount2, testPaymentMethod1)

      val result = Await.result(spreedly.listAllTransactions(None, true), timeout)
      assert(result.isInstanceOf[Seq[SpreedlyTransactionResponse]])
      assert(result.nonEmpty)
      assert(result.map(_.getGatewayToken).contains(testGatewayAccount1.getToken))
      assert(result.map(_.getGatewayToken).contains(testGatewayAccount2.getToken))
      assert(result.map(_.getToken).contains(testPurchase1.getToken))
      assert(result.map(_.getToken).contains(testPurchase2.getToken))
      assert(result.map(_.getToken).contains(testPurchase3.getToken))
    }

    "refund a transaction" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val request = new SpreedlyTransactionRequest()
      request.setReferenceTransactionToken(testPurchase.getToken)
      request.setAmountInCents(100)
      request.setCurrencyCode("USD")

      val result = Await.result(spreedly.refundTransaction(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getAmountInCents == 100)
      assert(result.getTransactionType == SpreedlyTransactionType.CREDIT)
    }

    "partially refund a transaction" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val request = new SpreedlyTransactionRequest()
      request.setReferenceTransactionToken(testPurchase.getToken)
      request.setAmountInCents(50)
      request.setCurrencyCode("USD")

      val result = Await.result(spreedly.refundTransaction(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getAmountInCents == 50)
      assert(result.getTransactionType == SpreedlyTransactionType.CREDIT)
    }

    "refund can handle a negative amount by using the absolute value" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val request = new SpreedlyTransactionRequest()
      request.setReferenceTransactionToken(testPurchase.getToken)
      request.setAmountInCents(-50)
      request.setCurrencyCode("USD")

      val result = Await.result(spreedly.refundTransaction(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getAmountInCents == 50)
      assert(result.getTransactionType == SpreedlyTransactionType.CREDIT)
    }

    "authorize a transaction" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount

      val request = new SpreedlyTransactionRequest()
      request.setGatewayAccountToken(testGatewayAccount.getToken)
      request.setPaymentMethodToken(testPaymentMethod.getToken)
      request.setAmountInCents(50)
      request.setCurrencyCode("USD")

      val result = Await.result(spreedly.authorizeTransaction(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.isOnTestGateway)
      assert(result.succeeded)
      assert(result.getAmountInCents == 50)
      assert(result.getTransactionType == SpreedlyTransactionType.AUTHORIZATION)
    }

    "capture a transaction" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val request = new SpreedlyTransactionRequest()
      request.setReferenceTransactionToken(testPurchase.getToken)
      request.setAmountInCents(50)
      request.setCurrencyCode("USD")

      val result = Await.result(spreedly.captureTransaction(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getAmountInCents == 50)
      assert(result.getTransactionType == SpreedlyTransactionType.CAPTURE)
    }

    "general credit" in {
      val testGatewayAccount = createTestGatewayAccount
      val testPaymentMethod = createTestPaymentMethod

      val request = new SpreedlyTransactionRequest()
      request.setAmountInCents(200)
      request.setCurrencyCode("USD")
      request.setGatewayAccountToken(testGatewayAccount.getToken)
      request.setPaymentMethodToken(testPaymentMethod.getToken)

      val result = Await.result(spreedly.generalCredit(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getAmountInCents == 200)
    }

    "get a transaction transcript" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchase = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val result = Await.result(spreedly.getTranscript(testPurchase.getToken), timeout)
      assert(result.isInstanceOf[String])
    }

    "make a reference purchase" in {
      val testPaymentMethod = createTestPaymentMethod
      val testGatewayAccount = createTestGatewayAccount
      val testPurchaseRef = createTestPurchase(testGatewayAccount, testPaymentMethod)

      val request = new SpreedlyTransactionRequest()
      request.setReferenceTransactionToken(testPurchaseRef.getToken)
      request.setAmountInCents(100)
      request.setCurrencyCode("USD")

      val result = Await.result(spreedly.purchase(request), timeout)
      assert(result.isInstanceOf[SpreedlyTransactionResponse])
      assert(result.succeeded)
      assert(result.isOnTestGateway)
      assert(result.getAmountInCents == 100)
    }
  }
}
