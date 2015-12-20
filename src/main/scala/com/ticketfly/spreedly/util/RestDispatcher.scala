package com.ticketfly.spreedly.util

import scala.concurrent.Future

trait RestDispatcher {
  def get[T <: AnyRef](url: String, responseType: Class[T], queryParams: Map[String, String]): Future[T]
  def options[T <: AnyRef](url: String, responseType: Class[T], queryParams: Map[String, String]): Future[T]
  def put[T <: AnyRef](url: String, responseType: Class[T], content: Option[AnyRef], queryParams: Map[String, String]): Future[T]
  def post[T <: AnyRef](url: String, responseType: Class[T], content: Option[AnyRef], queryParams: Map[String, String]): Future[T]
  def delete[T <: AnyRef](url: String, responseType: Class[T], queryParams: Map[String, String]): Future[T]
}
