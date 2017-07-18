package com.mguid.akka.sample.network

import java.net.InetSocketAddress

import akka.actor.{Actor, ActorLogging, Props}
import akka.event.LoggingReceive
import akka.io.{IO, Tcp}
import com.mguid.akka.sample.RequestTCPHandler

class TCPServer(host: String, port: Int) extends Actor with ActorLogging {

  import akka.io.Tcp._
  import context.system

  IO(Tcp) ! Bind(self, new InetSocketAddress(host, port))

  override def receive: Receive = LoggingReceive {
    case b @ Bound(localAddress) => context.parent ! b
    case CommandFailed(_: Bind) => context stop self
    case c @ Connected(remote, local) =>
      val handler = context.actorOf(Props[RequestTCPHandler])
      val connection = sender()
      connection ! Register(handler, keepOpenOnPeerClosed = true)
  }
}
