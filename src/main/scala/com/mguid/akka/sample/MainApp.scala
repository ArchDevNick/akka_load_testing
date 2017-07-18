package com.mguid.akka.sample

import java.net.{InetAddress, InetSocketAddress}

import akka.actor.{ActorSystem, Props}
import com.mguid.akka.sample.network.TCPServer
import com.typesafe.config.ConfigFactory

object MainApp {

  val config: com.typesafe.config.Config = ConfigFactory.load()
  val system: ActorSystem = ActorSystem(config.getString("akka.name"))
  val host: String = config.getString("tcp.host")
  val port: Int = config.getInt("tcp.port")

  def main(args: Array[String]) {
    args.foreach(process)
  }

  private def process: String => Unit = {
    case "server" => system.actorOf(Props(classOf[TCPServer], host, port))
    case "client" =>
      val address = new InetSocketAddress(InetAddress.getByName(host), port)
      val client = system.actorOf(TCPClient.props(address))
      client ! TCPRequest(1, "LogIn").binary
      client ! TCPRequest(2, "ToDoSomthing").binary
      client ! TCPRequest(3, "LogOut").binary
  }
}
