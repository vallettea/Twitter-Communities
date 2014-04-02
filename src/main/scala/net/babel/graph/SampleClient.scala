package net.babel.graph

import akka.actor.{ ActorRef, Actor, Props, ActorSystem, FSM }
import scala.io.Source

object SampleClient extends App {

  // reading credentials
  val lines = Source.fromFile(s"access.txt").getLines().toList

  // creating actors
  val system = ActorSystem("SampleClient")
  val twitterSource = system.actorOf(Props(classOf[TwitterSource]), name = "twitterSource")
  val graphActor = system.actorOf(Props(classOf[GraphActor]), name = "graphActor")

  twitterSource ! Authenticate(lines(0), lines(1), lines(2), lines(3))

  // initial roots
  val roots = List("vallettea")
  twitterSource ! FetchUser("vallettea")

}
