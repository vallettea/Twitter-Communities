package net.babel.graph

import net.babel.model._
import net.babel.model.TwitterJsonProtocol._

import akka.actor.{ ActorRef, Actor, Props, ActorSystem, FSM }
import org.apache.commons.codec.binary.Base64

import scala.concurrent.{ Future, future, ExecutionContext, Await }
import scala.concurrent.duration.DurationInt
import scala.util.{ Success, Failure }

import spray.http._
import spray.json.DefaultJsonProtocol
import spray.httpx.encoding.{ Gzip, Deflate }
import spray.httpx.SprayJsonSupport
import spray.client.pipelining._
import SprayJsonSupport._

sealed trait Messages
case class FetchUser(userName: String) extends Messages
case class FetchUserTweets(userName: String) extends Messages
case class FetchFriendIds(userId: Long) extends Messages
case class UserTweets(tweets: List[Tweet]) extends Messages
case class FriendIds(userId: Long, friendIds: List[Long]) extends Messages
case class Authenticate(arg1: String, arg2: String, arg3: String, arg4: String) extends Messages

sealed trait sourceState
case object Authenticated extends sourceState
case object Unauthenticated extends sourceState

sealed trait ClientData
case object NoData extends ClientData
case class TokenData(token: String) extends ClientData

class TwitterSource extends Actor with FSM[sourceState, ClientData] {

  import ExecutionContext.Implicits.global
  val twitterBaseUrl = "https://api.twitter.com"

  def pipeline(token: String): HttpRequest => Future[HttpResponse] = {
    (
      addHeader("Authorization", s"Bearer $token")
      ~> encode(Gzip)
      ~> sendReceive
      ~> decode(Deflate)
    )
  }

  startWith(Unauthenticated, NoData)

  when(Authenticated) {

    case Event(FetchFriendIds(userId), TokenData(token)) =>
      println("Fetching friend ids from " + userId.toString)
      val response = pipeline(token) {
        Get(s"$twitterBaseUrl/1.1/friends/ids.json?user_id=$userId&count=5000")
      }
      val users = Await.result(response, 5 seconds)
      println(users.status)
      val friendIds = users ~> unmarshal[Friends]
      sender ! FriendIds(userId, friendIds.ids)
      stay using TokenData(token)

    case Event(FetchUser(userName), TokenData(token)) =>
      println("Fetching info from " + userName)
      val response = pipeline(token) {
        Get(s"$twitterBaseUrl/1.1/users/lookup.json?screen_name=$userName")
      }
      val user = Await.result(response, 5 seconds)
      println(user.entity)
      println(user ~> unmarshal[TwitterUser])
      stay using TokenData(token)

    case Event(FetchUserTweets(userName), TokenData(token)) =>
      println("Fetching tweets from " + userName)
      val response = pipeline(token) {
        Get(s"$twitterBaseUrl/1.1/statuses/user_timeline.json?screen_name=$userName&count=50&include_rts=true&exclude_replies=true")
      }
      val tweets = Await.result(response, 5 seconds) ~> unmarshal[List[Tweet]]
      sender ! UserTweets(tweets)

      stay using TokenData(token)
  }

  when(Unauthenticated) {

    case Event(Authenticate(consumerKey, consumerSecret, arg3, arg4), NoData) =>

      val credentials = Base64.encodeBase64String(s"$consumerKey:$consumerSecret".getBytes())

      val pipeline: HttpRequest => Future[TwitterToken] = (
        addHeader("Authorization", s"Basic $credentials")
        ~> encode(Gzip)
        ~> sendReceive
        ~> decode(Deflate)
        ~> unmarshal[TwitterToken]
      )
      val response = pipeline {
        Post(s"$twitterBaseUrl/oauth2/token", FormData(Map("grant_type" -> "client_credentials")))
      }
      val token = Await.result(response, 10 seconds).access_token

      println("Moving to Authenticated.")
      goto(Authenticated) using TokenData(token)
  }

}