package com.ticketfly.spreedly.fixtures

import cc.protea.spreedly.model._
import java.util.Random

object TestCreditCards {
  private def setCardAttributes(card: SpreedlyCreditCard): SpreedlyCreditCard = {
    card.setEmail("testEmail@test.com")
    card.setData("testCardData")
    card.setMonth(11)
    card.setYear(2018)
    card.setFirstName("Joe")
    card.setLastName("Johnson")
    card.setVerificationValue("123")
    card
  }

  private def randomCardFromList(options: List[SpreedlyCreditCard]): SpreedlyCreditCard = {
    val random = new Random(System.currentTimeMillis())
    val random_index = random.nextInt(options.length)
    options(random_index)
  }

  def randomValid: SpreedlyCreditCard = {
    randomCardFromList(List(amexValid, visaValid, mastercardValid))
  }

  def randomInvalid: SpreedlyCreditCard = {
    randomCardFromList(List(amexInvalid, visaInvalid, mastercardInvalid))
  }

  def amexValid: SpreedlyCreditCard = {
    val card = new SpreedlyCreditCard()
    card.setCardType(SpreedlyCardType.AMEX)
    card.setNumber("378282246310005")
    setCardAttributes(card)
  }

  def amexInvalid: SpreedlyCreditCard = {
    val card = new SpreedlyCreditCard()
    card.setCardType(SpreedlyCardType.AMEX)
    card.setNumber("371449635398431")
    setCardAttributes(card)
  }

  def visaValid: SpreedlyCreditCard = {
    val card = new SpreedlyCreditCard()
    card.setCardType(SpreedlyCardType.VISA)
    card.setNumber("4111111111111111")
    setCardAttributes(card)
  }

  def visaInvalid: SpreedlyCreditCard = {
    val card = new SpreedlyCreditCard()
    card.setCardType(SpreedlyCardType.VISA)
    card.setNumber("4012888888881881")
    setCardAttributes(card)
  }

  def mastercardValid: SpreedlyCreditCard = {
    val card = new SpreedlyCreditCard()
    card.setCardType(SpreedlyCardType.MASTERCARD)
    card.setNumber("5555555555554444")
    setCardAttributes(card)
  }

  def mastercardInvalid: SpreedlyCreditCard = {
    val card = new SpreedlyCreditCard()
    card.setCardType(SpreedlyCardType.MASTERCARD)
    card.setNumber("5105105105105100")
    setCardAttributes(card)
  }

}


