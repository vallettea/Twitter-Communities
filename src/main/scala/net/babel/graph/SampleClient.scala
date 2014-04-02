package net.babel.graph

import net.babel.model._
import akka.actor.{ ActorRef, Actor, Props, ActorSystem, FSM }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.io.Source

object SampleClient extends App {

  // reading credentials
  val lines = Source.fromFile(s"access.txt").getLines().toList

  // creating actors
  val system = ActorSystem("SampleClient")
  val monitor = system.actorOf(Props(classOf[Monitor]), name = "monitor")
  val twitterSource = system.actorOf(Props(classOf[TwitterSource]), name = "twitterSource")
  val graphActor = system.actorOf(Props(classOf[GraphActor]), name = "graphActor")

  twitterSource ! Authenticate(lines(0), lines(1), lines(2), lines(3))

  // initial roots
  val roots = List("vallettea", "davidbruant")
  roots.foreach(screenName => { monitor ! FetchUserTweets(screenName) })

  class Monitor extends Actor {

    def receive = {

      case FetchUserTweets(user) => { twitterSource ! FetchUserTweets(user) }
      case UserTweets(tweets) => {
        if (tweets.size > 0) {
          val user = tweets.head.user
          user match {
            case Some(user) => user.id match {
              case Some(id) => twitterSource ! FetchFriendIds(id)
              case None => println("no id")
            }
            case None => println("no user")
          }
        }
      }
      case FriendIds(userId, friendIds) => {
        println("receive")
        graphActor ! FriendIds(userId, friendIds)
      }

    }

  }

}
