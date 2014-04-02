package net.babel.api

import net.babel.graph._

import spray.routing.SimpleRoutingApp
import spray.http._

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.ExecutionContext

import akka.actor.{ Actor, ActorRef, ActorSystem, ActorLogging }
import akka.pattern.ask
import akka.actor.Props
import akka.util.Timeout

object Api extends App with SimpleRoutingApp {

  println("Chunk size is " + com.typesafe.config.ConfigFactory.load().getString("spray.can.parsing.max-chunk-size"))
  println("Log level is " + com.typesafe.config.ConfigFactory.load().getString("akka.loglevel"))

  //---------------------------------------------//
  //------------- SPRAY HTTP SERVER -------------//
  //---------------------------------------------//

  implicit val timeout = Timeout(30 seconds)
  implicit val ec = ExecutionContext.Implicits.global
  implicit val system = ActorSystem("twitter-communities-api")

  val graphActor = system.actorOf(Props(classOf[GraphActor]), name = "graphActor")

  val completeCompressedCrossOrigin = {
    respondWithHeader(HttpHeaders.RawHeader("Access-Control-Allow-Origin", "*")) &
      compressResponse() &
      complete
  }

  startServer(interface = "0.0.0.0", port = 5678) {

    pathPrefix("v1") {

      path("example") { //localhost/v2/routing
        get {
          parameters("parameter1", "mode" ? "3") { (parameter1, mode) =>

            completeCompressedCrossOrigin {
              StringUtils.toJerksonJson {
                println("to be done, sorry")
              }
            }
          }
        }
      } ~
        path("allgraph") { //localhost/v2/allgraph
          get {

            val future = graphActor ? FetchAllGraph
            val answer = Await.result(future, timeout.duration).asInstanceOf[GraphAnswerMap]
            val graphOutput = GraphOutput.fromData(answer)

            completeCompressedCrossOrigin {
              StringUtils.toJerksonJson {
                graphOutput
              }
            }
          }
        }

    }
  }
}