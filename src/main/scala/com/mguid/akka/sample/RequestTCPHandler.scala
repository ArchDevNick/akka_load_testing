package com.mguid.akka.sample

import akka.actor.{Actor, ActorLogging}
import akka.event.LoggingReceive
import akka.io.Tcp.Event
import akka.util.{ByteString, ByteStringBuilder}

import scala.util.{Failure, Success}

class RequestTCPHandler extends Actor with ActorLogging {

  case object Ack extends Event

  import akka.io.Tcp._

  def receive: Receive = LoggingReceive {
    case Received(data) =>
      val response: ByteString = RequestParser.parse(data) match {
        case Success(requests) =>
          println(s"Request: $requests")
          requests.foldLeft(new ByteStringBuilder){(res, c) => res.append(c.binary)}.result()
        case Failure(e) =>
          println(s"Request: ${e.getMessage}")
          TCPRequest(-1, e.getMessage).binary
      }
      sender() ! Write(response)
    case PeerClosed => context stop self
  }
}
