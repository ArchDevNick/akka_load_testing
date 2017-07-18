package com.mguid.akka.sample

import akka.util.{ByteString, ByteStringBuilder}

case class TCPRequest(number: Short, body: String) {

  def binary: ByteString = {
    implicit val order = java.nio.ByteOrder.nativeOrder
    (new ByteStringBuilder).putShort(number).putInt(body.size).append(ByteString.apply(body)).result()
  }

}
