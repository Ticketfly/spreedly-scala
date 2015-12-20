package com.ticketfly.spreedly.mocks

import org.specs2.mock.Mockito
import scala.collection.JavaConverters._
import cc.protea.spreedly.model._

trait MockDomainObjects extends Mockito {
  def mockPaymentMethod: SpreedlyPaymentMethod = {
    val paymentMethod = new SpreedlyPaymentMethod()
    paymentMethod.setToken("testPaymentToken")
    paymentMethod.setEmail("testPaymentEmail")
    paymentMethod.setFirstName("testFirstName")
    paymentMethod.setLastName("testLastName")
    paymentMethod.setCardType(SpreedlyCardType.VISA)
    paymentMethod
  }

  def mockGatewayCharacteristics: SpreedlyGatewayCharacteristics = {
    val characteristics = new SpreedlyGatewayCharacteristics()
    characteristics.setSupportsAuthorize(true)
    characteristics.setSupportsPurchase(true)
    characteristics
  }

  def mockGatewayCredential: SpreedlyGatewayCredential = {
    val cred = new SpreedlyGatewayCredential()
    cred.setName("testCredentialName")
    cred.setValue("testCredentialValue")
    cred
  }

  def mockGatewayAccount: SpreedlyGatewayAccount = {
    val acc = new SpreedlyGatewayAccount()
    acc.setToken("testAccountToken")
    acc.setName("testAccountName")
    acc.setGatewayType("testAccountType")
    acc.setPaymentMethods(List(SpreedlyGatewayPaymentMethod.CREDIT_CARD).asJava)
    acc.setRedacted(false)
    acc.setCharacteristics(mockGatewayCharacteristics)
    acc.setCredentials(List(mockGatewayCredential).asJava)
    acc.setCreatedOn(new java.util.Date())
    acc.setUpdatedOn(new java.util.Date())

    acc
  }

  def mockTransactionRequest: SpreedlyTransactionRequest = {
    val req = new SpreedlyTransactionRequest()
    req.setGatewayAccountToken("testAccountToken")
    req.setAmountInCents(100)
    req.setCurrencyCode("US")
    req.setRetainOnSuccess(true)
    req.setDescription("testDescription")
    req.setPaymentMethodToken("testPaymentMethodToken")
    req.setOrderId("testOrderId")

    req
  }

  def mockTransactionResponse: SpreedlyTransactionResponse = {
    val res = new SpreedlyTransactionResponse()
    res.setToken("testTransactionResponseToken")
    res.setSucceeded(true)
    res.setDescription("testDescription")
    res.setCurrencyCode("US")
    res.setGatewayTransactionId("testGatewayTransactionId")
    res.setGatewayToken("testAccountToken")
    res.setAmountInCents(100)
    res.setCreatedOn(new java.util.Date())
    res.setUpdatedOn(new java.util.Date())
    res.setTransactionType(SpreedlyTransactionType.CREDIT)

    res
  }

}
