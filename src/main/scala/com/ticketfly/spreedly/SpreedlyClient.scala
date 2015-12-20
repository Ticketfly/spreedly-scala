package com.ticketfly.spreedly

import akka.actor.ActorSystem
import cc.protea.spreedly.model._
import cc.protea.spreedly.model.internal._
import com.ticketfly.spreedly.internal.SpreedlyPaymentMethodCreateRequest
import scala.concurrent.Future
import scala.collection.JavaConverters._
import scala.collection.mutable

trait SpreedlyGatewayProviderActions {
  def listGatewayProviders: Future[Seq[SpreedlyGatewayProvider]]
  def getGatewayProvider(name: String): Future[Option[SpreedlyGatewayProvider]]
}

trait SpreedlyGatewayAccountActions {
  def createGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyGatewayAccount]
  def getGatewayAccount(token: String): Future[SpreedlyGatewayAccount]
  def listGatewayAccounts(sinceToken: Option[String], desc: Boolean, retained: Boolean): Future[Seq[SpreedlyGatewayAccount]]
  def listGatewayTransactions(gatewayToken: String, sinceToken: Option[String], desc: Boolean): Future[Seq[SpreedlyTransactionResponse]]
  def purchase(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
  def purchase(gatewayAccountToken: String,
               paymentMethodToken: String,
               amountInCents: Int,
               currencyCode: String,
               retain: Boolean): Future[SpreedlyTransactionResponse]
  def redactGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyTransactionResponse]
  def retainGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyTransactionResponse]
  def updateGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyGatewayAccount]
  def verifyGatewayAccount(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
}

trait SpreedlyPaymentMethodActions {
  def createPaymentMethod(creditCard: SpreedlyCreditCard): Future[SpreedlyTransactionResponse]
  def getPaymentMethod(paymentMethodToken: String): Future[SpreedlyPaymentMethod]
  def listPaymentMethods(sinceToken: Option[String], desc: Boolean, retained: Boolean): Future[Seq[SpreedlyPaymentMethod]]
  def listPaymentMethodTransactions(paymentMethodToken: String, sinceToken: Option[String], desc: Boolean): Future[Seq[SpreedlyTransactionResponse]]
  def redactPaymentMethod(paymentMethodToken: String, gatewayAccountToken: Option[String]): Future[SpreedlyTransactionResponse]
  def recachePaymentMethod(paymentMethodToken: String, verificationValue: String): Future[SpreedlyTransactionResponse]
  def recachePaymentMethod(paymentMethod: SpreedlyPaymentMethod): Future[SpreedlyTransactionResponse]
  def retainPaymentMethod(paymentMethodToken: String): Future[SpreedlyTransactionResponse]
  def updatePaymentMethod(paymentMethod: SpreedlyPaymentMethod): Future[SpreedlyPaymentMethod]
}

trait SpreedlyTransactionActions {
  def authorizeTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
  def captureTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
  def generalCredit(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
  def getTransaction(token: String): Future[SpreedlyTransactionResponse]
  def getTranscript(token: String): Future[String]
  def listAllTransactions(sinceToken: Option[String], desc: Boolean): Future[Seq[SpreedlyTransactionResponse]]
  def purchaseReference(referenceTransactionToken: String, amountInCents: Integer, currencyCode: String): Future[SpreedlyTransactionResponse]
  def refundTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
  def voidTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse]
}


class SpreedlyClient(config: SpreedlyConfiguration)(implicit system: ActorSystem)
  extends SpreedlyGatewayProviderActions
  with SpreedlyGatewayAccountActions
  with SpreedlyPaymentMethodActions
  with SpreedlyTransactionActions {

  import system.dispatcher

  protected val rest = new SpreedlyRestDispatcher(config)

  private def gatewayPost(request: SpreedlyTransactionRequest, url: String): Future[SpreedlyTransactionResponse] = {
    rest.post(s"gateways/${request.getGatewayAccountToken}/$url", classOf[SpreedlyTransactionResponse], Some(request))
  }

  private def transactionPost(request: SpreedlyTransactionRequest, url: String): Future[SpreedlyTransactionResponse] = {
    rest.post(s"transactions/${request.getReferenceTransactionToken}/$url", classOf[SpreedlyTransactionResponse], Some(request))
  }

  private def paramsToMap(sinceToken: Option[String] = None, desc: Boolean = false, retained: Boolean = false): Map[String, String] = {
    val pMap = mutable.Map.empty[String, String]
    if (sinceToken.nonEmpty) pMap("since_token") = sinceToken.get
    if (desc) pMap("order") = "desc"
    if (retained) pMap("filter") = "retained"

    pMap.toMap
  }


  /******************
   * Gateway Providers
   ******************/

  def getGatewayProvider(name: String): Future[Option[SpreedlyGatewayProvider]] = {
    listGatewayProviders.map(_.find(_.getName.toLowerCase == name.toLowerCase))
  }

  def listGatewayProviders: Future[Seq[SpreedlyGatewayProvider]] = {
    rest.options[SpreedlyGatewayProviderResponse]("gateways", classOf[SpreedlyGatewayProviderResponse])
      .map(_.gateways.asScala)
  }


  /******************
   * Gateway Accounts
   ******************/

  private def buildGatewayAccountUpdate(account: SpreedlyGatewayAccount): SpreedlyGatewayAccountUpdate = {
    val updateRequest: SpreedlyGatewayAccountUpdate = new SpreedlyGatewayAccountUpdate(account)
    updateRequest
  }

  def createGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyGatewayAccount] = {
    rest.post("gateways", classOf[SpreedlyGatewayAccount], Some(buildGatewayAccountUpdate(account)))
  }

  def getGatewayAccount(token: String): Future[SpreedlyGatewayAccount] = {
    rest.get(s"gateways/$token", classOf[SpreedlyGatewayAccount])
  }

  def listGatewayAccounts(sinceToken: Option[String] = None, desc: Boolean = false, retained: Boolean = false): Future[Seq[SpreedlyGatewayAccount]] = {
    rest.get[SpreedlyGatewayAccountResponse]("gateways", classOf[SpreedlyGatewayAccountResponse], paramsToMap(sinceToken, desc))
      .map(_.gateways.asScala)
  }

  def listGatewayTransactions(gatewayToken: String, sinceToken: Option[String] = None, desc: Boolean = false): Future[Seq[SpreedlyTransactionResponse]] = {
    rest.get[SpreedlyTransactionListResponse](
      s"gateways/$gatewayToken/transactions",
      classOf[SpreedlyTransactionListResponse],
      paramsToMap(sinceToken, desc)
    ).map(_.transactions.asScala)
  }

  /**
   * Purchase using gateway account token, payment method token, amount, and currency code
   */
  def purchase(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    if (request.getReferenceTransactionToken != null) {
      purchaseReference(request.getReferenceTransactionToken, request.getAmountInCents, request.getCurrencyCode)
    } else {
      gatewayPost(request, "purchase")
    }
  }

  /**
   * Helper for creating a standard gateway purchase
   */
  def purchase(gatewayAccountToken: String,
               paymentMethodToken: String,
               amountInCents: Int,
               currencyCode: String = "USD",
               retain: Boolean = true): Future[SpreedlyTransactionResponse] = {
    val request = new SpreedlyTransactionRequest()
    request.setGatewayAccountToken(gatewayAccountToken)
    request.setPaymentMethodToken(paymentMethodToken)
    request.setAmountInCents(amountInCents)
    request.setCurrencyCode(currencyCode)
    request.setRetainOnSuccess(retain)
    purchase(request)
  }

  def redactGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyTransactionResponse] = {
    rest.put(s"gateways/${account.getToken}/redact", classOf[SpreedlyTransactionResponse])
  }

  def retainGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyTransactionResponse] = {
    rest.put(s"gateways/${account.getToken}/retain", classOf[SpreedlyTransactionResponse])
  }

  /**
   * Update credentials for a gateway account
   */
  def updateGatewayAccount(account: SpreedlyGatewayAccount): Future[SpreedlyGatewayAccount] = {
    rest.put(s"gateways/${account.getToken}", classOf[SpreedlyGatewayAccount], Some(buildGatewayAccountUpdate(account)))
  }

  def verifyGatewayAccount(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    gatewayPost(request, "verify")
  }



  /******************
   * Payment Methods
   ******************/

  def createPaymentMethod(creditCard: SpreedlyCreditCard): Future[SpreedlyTransactionResponse] = {
    val createRequest = new SpreedlyPaymentMethodCreateRequest()
    createRequest.creditCard = creditCard
    createRequest.data = creditCard.data
    createRequest.email = creditCard.email
    rest.post[SpreedlyTransactionResponse](s"payment_methods", classOf[SpreedlyTransactionResponse], Some(createRequest))
  }

  def getPaymentMethod(paymentMethodToken: String): Future[SpreedlyPaymentMethod] = {
    rest.get(s"payment_methods/$paymentMethodToken", classOf[SpreedlyPaymentMethod])
  }

  def listPaymentMethods(sinceToken: Option[String] = None, desc: Boolean = false, retained: Boolean = false): Future[Seq[SpreedlyPaymentMethod]] = {
    rest.get("payment_methods", classOf[SpreedlyPaymentMethodListResponse], paramsToMap(sinceToken, desc, retained))
      .map(_.paymentMethods.asScala)
  }

  def listPaymentMethodTransactions(paymentMethodToken: String,
                                    sinceToken: Option[String] = None,
                                    desc: Boolean = false): Future[Seq[SpreedlyTransactionResponse]] = {
    rest.get(
      s"payment_methods/$paymentMethodToken/transactions",
      classOf[SpreedlyTransactionListResponse],
      paramsToMap(sinceToken, desc)
    ).map(_.transactions.asScala)
  }

  def redactPaymentMethod(paymentMethodToken: String, gatewayAccountToken: Option[String] = None): Future[SpreedlyTransactionResponse] = {
    val redactRequest: Option[SpreedlyPaymentMethodUpdate] = if (gatewayAccountToken.nonEmpty) {
      val redactRequestContent = new SpreedlyPaymentMethodUpdate()
      redactRequestContent.gatewayAccountToken = gatewayAccountToken.get
      Some(redactRequestContent)
    } else None

    rest.put(s"payment_methods/$paymentMethodToken/redact", classOf[SpreedlyTransactionResponse], redactRequest)
  }

  def recachePaymentMethod(paymentMethodToken: String, verificationValue: String): Future[SpreedlyTransactionResponse] = {
    val request = new SpreedlyPaymentMethod()
    request.setToken(paymentMethodToken)
    request.setVerificationValue(verificationValue)
    recachePaymentMethod(request)
  }

  def recachePaymentMethod(paymentMethod: SpreedlyPaymentMethod): Future[SpreedlyTransactionResponse] = {
    rest.post(
      s"payment_methods/${paymentMethod.getToken}/recache",
      classOf[SpreedlyTransactionResponse],
      Some(paymentMethod)
    )
  }

  def retainPaymentMethod(paymentMethodToken: String): Future[SpreedlyTransactionResponse] = {
    rest.put(s"payment_methods/$paymentMethodToken/retain", classOf[SpreedlyTransactionResponse])
  }

  def updatePaymentMethod(paymentMethod: SpreedlyPaymentMethod): Future[SpreedlyPaymentMethod] = {
    rest.put(s"payment_methods/${paymentMethod.getToken}", classOf[SpreedlyPaymentMethod], Some(paymentMethod))
  }


  /******************
   * Transactions
   ******************/

  /**
   * Contact the payment gateway to verify if funds can be transferred in a transaction.
   * Once authorized, the transaction can be performed via capture.
   */
  def authorizeTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    gatewayPost(request, "authorize")
  }

  /**
   * Perform a fund transfer which has already been authorized.
   */
  def captureTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    transactionPost(request, "capture")
  }

  /**
   * Add funds to a payment method.  Not allowed by all gateways.
   */
  def generalCredit(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    gatewayPost(request, "general_credit")
  }

  def getTransaction(token: String): Future[SpreedlyTransactionResponse] = {
    rest.get(s"transactions/$token", classOf[SpreedlyTransactionResponse])
  }

  def getTranscript(token: String): Future[String] = {
    rest.get(s"transactions/$token/transcript", classOf[String])
  }

  def listAllTransactions(sinceToken: Option[String] = None, desc: Boolean = false): Future[Seq[SpreedlyTransactionResponse]] = {
    rest.get[SpreedlyTransactionListResponse](
      "transactions",
      classOf[SpreedlyTransactionListResponse],
      paramsToMap(sinceToken, desc)
    ).map(_.transactions.asScala)
  }

  /**
   * Make a reference purchase, for the case where cards are not retained
   * @see https://docs.spreedly.com/guides/using-payment-methods/#reference-purchases
   */
  def purchaseReference(referenceTransactionToken: String, amountInCents: Integer, currencyCode: String): Future[SpreedlyTransactionResponse] = {
    val request: SpreedlyTransactionRequest = new SpreedlyTransactionRequest()
    request.setReferenceTransactionToken(referenceTransactionToken)
    request.setAmountInCents(amountInCents)
    request.setCurrencyCode(currencyCode)
    transactionPost(request, "purchase")
  }

  def refundTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    if (request.getAmountInCents < 0) request.setAmountInCents(Math.abs(request.getAmountInCents))

    transactionPost(request, "credit")
  }

  def voidTransaction(request: SpreedlyTransactionRequest): Future[SpreedlyTransactionResponse] = {
    transactionPost(request, "void")
  }


}
