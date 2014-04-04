package net.babel.graph

import akka.actor._
import akka.cluster.Cluster
import akka.cluster.ClusterEvent._
import akka.contrib.pattern.DistributedPubSubExtension
import akka.contrib.pattern.DistributedPubSubMediator.{ Publish, Subscribe }

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, ExecutionContext, Future }
import scala.concurrent.duration._
import akka.pattern.ask
import akka.util.Timeout
import com.thinkaurelius.titan.core.{ TitanGraph, TitanFactory, TitanVertex, TitanEdge, TitanException }
import com.tinkerpop.blueprints.{ Direction, Vertex => BluePrintVertex, Edge => BluePrintEdge }
import scala.collection.JavaConversions._
import java.lang.{ Long => JLong, Double => JDouble }
/**
 * GraphTwitter
 * contextual
 *
 * Created by vallettea on 19/03/14.
 * Copyright (c) 2014 Snips. All rights reserved.
 */

case class GetUsers(usersIds: List[String])
case object FetchAllGraph
case class GraphAnswerMap(data: Map[_, _])

class GraphActor extends Actor with ActorLogging {

  val conf = new org.apache.commons.configuration.BaseConfiguration()
  conf.setProperty("storage.backend", "persistit")
  conf.setProperty("storage.directory", "/tmp/twitter_graph/")

  implicit val graph = TitanFactory.open(conf).asInstanceOf[TitanGraph]
  try {
    graph.makeKey("uid").dataType(classOf[JLong]).indexed(classOf[BluePrintVertex]).unique().make()
    graph.commit()
  } catch { case e: Exception => println("Unable to create index (graph not empty).") }

  def receive = {

    case FriendIds(userId, friendIds) => insertUsers(userId, friendIds)
    case GetUsers(usersIds) => sender ! getUsers(usersIds)
    case FetchAllGraph => sender ! GraphAnswerMap(Map("vertices" -> getAllVertices, "edges" -> getAllEdges))

  }

  def fetchOrAddNode(uid: Long): TitanVertex = {
    val query = graph.getVertices("uid", uid)
    if (query.size > 0) {
      query.head.asInstanceOf[TitanVertex]
    } else {
      val vertex = graph.addVertex(null)
      vertex.setProperty("uid", uid)
      graph.commit
      vertex.asInstanceOf[TitanVertex]
    }
  }

  def fetchOrAddEdge(fromVertex: TitanVertex, toVertex: TitanVertex): TitanEdge = {
    // a map: outVertex => Edge
    // val outVertices = fromVertex.getEdges(Direction.OUT).toList.asInstanceOf[List[TitanEdge]]
    //   .map(edge => (edge.getVertex(Direction.OUT).asInstanceOf[TitanVertex], edge)).toMap
    // if (outVertices.keySet contains toVertex) {
    //   outVertices(toVertex)
    // } else {
    val edge = graph.addEdge(null, fromVertex, toVertex, "follows")
    graph.commit
    edge.asInstanceOf[TitanEdge]
    // }
  }

  def insertUsers(userId: Long, friendIds: List[Long])(implicit graph: TitanGraph) {
    println(s"linking $userId to ${friendIds.size} people")
    val userVertex = fetchOrAddNode(userId)
    friendIds.map(friendId => {
      val friendVertex = fetchOrAddNode(friendId)
      fetchOrAddEdge(userVertex, friendVertex)
    })
  }

  def getAllVertices: List[Long] = {
    graph.query.vertices.toList.map(vertex => vertex.getProperty[String]("uid").toLong)
  }

  def getAllEdges: List[(Long, Long)] = {
    graph.query.edges.toList
      .map(edge => (edge.getVertex(Direction.OUT).getProperty[String]("uid").toLong, edge.getVertex(Direction.IN).getProperty[String]("uid").toLong))
  }

  def getUsers(usersIds: List[String])(implicit graph: TitanGraph) = {
    println
  }

}