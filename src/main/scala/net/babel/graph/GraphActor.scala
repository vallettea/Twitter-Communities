package net.babel.graph

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{ Publish, Subscribe }
import com.thinkaurelius.titan.core.{ TitanFactory, TitanGraph }
import com.thinkaurelius.titan.core.attribute.Geoshape
import com.tinkerpop.blueprints.{ Vertex => TinkerpopVertex }

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout

/**
 * GraphTwitter
 * contextual
 *
 * Created by vallettea on 19/03/14.
 * Copyright (c) 2014 Snips. All rights reserved.
 */

case class PersistUsers(users: List[String])
case class GetUsers(usersIds: List[String])

class GraphActor extends Actor with ActorLogging {

  val conf = new org.apache.commons.configuration.BaseConfiguration()
  conf.setProperty("storage.backend", "persistit")
  conf.setProperty("storage.directory", "/tmp/twitter_graph/")

  implicit val graph = TitanFactory.open(conf).asInstanceOf[TitanGraph]
  try {
    graph.makeKey("uid").dataType(classOf[String]).indexed(classOf[TinkerpopVertex]).unique().make()
    graph.commit()
  } catch { case e: Exception => }

  def receive = {

    case PersistUsers(users) => insertUsers(users)
    case GetUsers(usersIds) => sender ! getUsers(usersIds)

  }

  def insertUsers(users: List[String])(implicit graph: TitanGraph) {
    println
  }

  def getUsers(usersIds: List[String])(implicit graph: TitanGraph) = {
    println
  }

}