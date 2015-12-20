package com.ticketfly.spreedly

case class SpreedlyException(error: Exception, errorCode: String = "UNKNOWN", errorMessage: String = null) extends RuntimeException(errorCode)
