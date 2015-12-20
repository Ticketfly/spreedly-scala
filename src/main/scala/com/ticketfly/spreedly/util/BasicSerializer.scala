package com.ticketfly.spreedly.util

trait BasicSerializer {
  def deserialize[T <: AnyRef](content: String, responseType: Class[T]): T
  def serialize[T <: AnyRef](obj: T): String
}
