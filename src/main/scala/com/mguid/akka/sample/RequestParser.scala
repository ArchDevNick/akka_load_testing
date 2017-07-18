package com.mguid.akka.sample

import akka.util.ByteString

import scala.util.Try

object RequestParser {
  implicit val order = java.nio.ByteOrder.nativeOrder

  case class InvalidBodySizeException(expected: Int, actual: Int) extends Exception("Message was interrupted")

  def parse(data: ByteString): Try[Vector[TCPRequest]] = Try {
    val iterator = data.iterator
    val commandNumber: Short = iterator.getShort
    val bodySize: Int = iterator.getInt
    val body: ByteString = iterator.take(bodySize).toByteString
    if (bodySize != body.size) throw InvalidBodySizeException(bodySize, body.size)
    (TCPRequest(commandNumber, body.decodeString("utf-8")), iterator.toByteString)
  }.flatMap {
    case (r, bstring) if bstring.nonEmpty => parse(bstring).map(_ :+ r)
    case (r, _) => Try(Vector(r))
  }
}
