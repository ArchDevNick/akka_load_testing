package com.mguid.akka.sample

object Commands {
  sealed trait Message
  case class LogIn(name: String, pass: String) extends Message
  case class DoSomthing(action: String) extends Message
  case object LogOut extends Message
}
