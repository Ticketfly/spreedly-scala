package com.ticketfly.spreedly.util

import scala.reflect.ClassTag

trait BasicSerializer {
  def deserialize[T <: AnyRef : ClassTag](content: String): T
  def serialize[T <: AnyRef](obj: T): String
}
