package net.babel.graph

import net.babel.model._
import akka.actor.{ ActorRef, Actor, Props, ActorSystem, FSM }
import akka.pattern.ask
import akka.util.Timeout
import akka.routing.{ RoundRobinRouter }
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.io.Source

object SampleClient extends App {

  // reading credentials
  val lines = Source.fromFile(s"access.txt").getLines().toList

  // creating actors
  val system = ActorSystem("SampleClient")
  val monitor = system.actorOf(Props(classOf[Monitor]), name = "monitor")
  val graphActor = system.actorOf(Props(classOf[GraphActor]), name = "graphActor")

  val routees = lines.map { line =>
    val tt = system.actorOf(Props(classOf[TwitterSource]), name = ("twitterSource_" + line))
    tt ! Authenticate(line.split(":")(0), line.split(":")(1))
    tt
  }

  val routerProps = Props.empty.withRouter(RoundRobinRouter(routees = routees))
  val twitterSource = system.actorOf(routerProps)

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
        graphActor ! FriendIds(userId, friendIds)
        friendIds.foreach(id => { twitterSource ! FetchFriendIds(id) })
      }

    }

  }

}
