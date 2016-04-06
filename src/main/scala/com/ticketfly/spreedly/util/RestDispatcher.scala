package com.ticketfly.spreedly.util

import scala.concurrent.Future
import scala.reflect.ClassTag

trait RestDispatcher {
  def get[T <: AnyRef : ClassTag](url: String, queryParams: Map[String, String]): Future[T]
  def options[T <: AnyRef : ClassTag](url: String, queryParams: Map[String, String]): Future[T]
  def put[T <: AnyRef : ClassTag](url: String, content: Option[AnyRef], queryParams: Map[String, String]): Future[T]
  def post[T <: AnyRef : ClassTag](url: String, content: Option[AnyRef], queryParams: Map[String, String]): Future[T]
  def delete[T <: AnyRef : ClassTag](url: String, queryParams: Map[String, String]): Future[T]
}
