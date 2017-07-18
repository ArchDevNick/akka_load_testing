package com.mguid.akka.sample

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Stash}
import akka.event.LoggingReceive
import akka.io.{IO, Tcp}
import akka.util.ByteString

object TCPClient {
  def props(remote: InetSocketAddress) =
    Props(classOf[TCPClient], remote)
}

class TCPClient(remote: InetSocketAddress) extends Actor with ActorLogging with Stash {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Connect(remote)

  def receive = LoggingReceive {
    case CommandFailed(_: Connect) =>
      context stop self
    case c @ Connected(_, _) =>
      val connection = sender()
      connection ! Register(self, keepOpenOnPeerClosed = true)
      unstashAll()
      context.become(active(connection))
    case _: ByteString => stash()
  }

  def active(connection: ActorRef): Receive = LoggingReceive {
    case data: ByteString =>
      connection ! Write(data)
      context.become(waitForResponse, discardOld = false)
    case _: ConnectionClosed =>
      connection ! Close
      context stop self
  }

  def waitForResponse: Receive = LoggingReceive {
    case _: ByteString => stash()
    case Received(data) =>
      RequestParser.parse(data).getOrElse(Vector.empty).foreach{ m => println(s"Response: ${m.body}")}
      context.unbecome()
      unstashAll()
    case CommandFailed(w: Write) =>
      // O/S buffer was full
      println(s"Response: write failed")
    case _: ConnectionClosed => context stop self
  }
}
