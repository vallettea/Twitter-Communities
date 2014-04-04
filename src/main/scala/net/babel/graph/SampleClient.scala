package net.babel.graph

import net.babel.model._
import akka.actor.{ ActorRef, Actor, Props, ActorSystem, FSM }
import akka.pattern.ask
import akka.routing.RoundRobinRouter
import akka.util.Timeout
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import scala.io.Source

object SampleClient extends App {

  implicit val timeout: Timeout = Timeout(10 seconds)
  // reading credentials
  val lines = Source.fromFile("access.txt").getLines().toList

  // creating actors
  val system = ActorSystem("SampleClient")
  // val monitor = system.actorOf(Props(classOf[Monitor]), name = "monitor")
  //val twitterSource = system.actorOf(Props(classOf[TwitterSource]), name = "twitterSource")
  // val graphActor = system.actorOf(Props(classOf[GraphActor]), name = "graphActor") //.withRouter(RoundRobinRouter(nrOfInstances = 10)

  val actorRefs = lines.map { line =>
    val tt = system.actorOf(Props(classOf[TwitterSource]), name = ("twitterSource_" + line))
    tt ! Authenticate(line.split(":")(0), line.split(":")(1))
    tt
  }

  var friendsDescription = List[String]()

  var idx = 0
  var cursor: Option[Long] = None
  do {

    val friendListFuture = actorRefs(idx) ? FetchFriends(args(0), cursor)
    val friendList = Await.result(friendListFuture, timeout.duration).asInstanceOf[FriendList]

    friendsDescription ++= friendList.users.map(_.description.getOrElse("").replaceAll("\\s+", " "))

    cursor = Option(friendList.next_cursor)
    idx += 1
    if (idx == actorRefs.size) idx = 0

  } while (cursor.getOrElse(1) != 0)

  friendsDescription.takeRight(20).mkString(" ").split(" ").toList.filter(_.length > 3).groupBy(k => k).map(kv => kv._1 -> kv._2.size).toList.sortBy(_._2).foreach(println)

  // initial roots
  // val roots = List("vallettea", "davidbruant")
  // roots.foreach(screenName => { monitor ! FetchUserTweets(screenName) })

  // class Monitor extends Actor {

  //   def receive = {

  //     case FetchUserTweets(user) => { twitterSource ! FetchUserTweets(user) }
  //     case UserTweets(tweets) => {
  //       if (tweets.size > 0) {
  //         val user = tweets.head.user
  //         user match {
  //           case Some(user) => user.id match {
  //             case Some(id) => twitterSource ! FetchFriendIds(id)
  //             case None => println("no id")
  //           }
  //           case None => println("no user")
  //         }
  //       }
  //     }
  //     case FriendIds(userId, friendIds) => {
  //       graphActor ! FriendIds(userId, friendIds)
  //       friendIds.foreach(id => { twitterSource ! FetchFriendIds(id) })
  //     }

  //   }

  // }

}
