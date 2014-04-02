package net.babel.api

import net.babel.graph._

case class NodeOutput(index: Long)

case class EdgeOutput(
  source: Long,
  target: Long)

case class GraphOutput(
  nodes: List[Long],
  edges: List[EdgeOutput])

object GraphOutput {

  def fromData(answer: GraphAnswerMap): GraphOutput = {

    val data = answer.data.asInstanceOf[Map[String, Any]]
    val vertices = data("vertices").asInstanceOf[List[Long]]
    val edges = data("edges").asInstanceOf[List[(Long, Long)]]

    // one has to format edges 
    val verticeIndex = vertices.zipWithIndex.map({ case (v, i) => (v, i) }).toMap
    val edgesOutput = edges.map({
      case (to, from) => {
        val source = verticeIndex.getOrElse(to, -1)
        val target = verticeIndex.getOrElse(from, -1)
        if (Set(source, target) contains -1) None
        else Some(EdgeOutput(from, to))
      }
    }).flatten

    GraphOutput(vertices, edgesOutput)
  }

}