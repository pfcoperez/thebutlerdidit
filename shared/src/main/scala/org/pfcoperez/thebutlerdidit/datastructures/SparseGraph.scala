package org.pfcoperez.thebutlerdidit.datastructures

import SparseGraph._

class SparseGraph[NodeTag, EdgeTag] private (
    private val nodes: InternalRep[NodeTag, EdgeTag] = Map.empty
) {

  def getNodes: Set[NodeTag] =
    nodes.keySet

  def getEdges(from: NodeTag, to: NodeTag): Set[EdgeTag] =
    getEdges(from).getOrElse(to, Set.empty)

  def getEdges(from: NodeTag): Map[NodeTag, Set[EdgeTag]] =
    nodes.getOrElse(from, Map.empty)

  def +(node: NodeTag): SparseGraph[NodeTag, EdgeTag] =
    nodes.get(node).map(_ => this).getOrElse {
      new SparseGraph(nodes + (node -> Map.empty))
    }

  def addEdge(
      from: NodeTag,
      to: NodeTag,
      tag: EdgeTag
  ): SparseGraph[NodeTag, EdgeTag] = {
    val currentFromEdges = nodes.getOrElse(from, Map.empty)
    val currentEdges     = currentFromEdges.getOrElse(to, Set.empty)
    new SparseGraph(
      nodes + (
            from -> (currentFromEdges + (
                      to -> (currentEdges + tag)
                    ))
          )
    )
  }

  def +(edge: ((NodeTag, EdgeTag), NodeTag)): SparseGraph[NodeTag, EdgeTag] = {
    val ((from, tag), to) = edge
    addEdge(from, to, tag)
  }

  def renderGraphviz(
      withIsolatedNodes: Boolean = false,
      nodesAttributes: NodeTag => Seq[RenderAttribute] = _ => Seq.empty,
      edgesAttributes: EdgeTag => Seq[RenderAttribute] = _ => Seq.empty
  ): String = {

    def renderAttributes(attributes: Seq[RenderAttribute]): String =
      attributes
        .map { attribute =>
          import attribute._
          s"$key=$value"
        }
        .mkString("[ ", ",", " ]")

    val nodesStrs = nodes.keys.flatMap { nodeKey =>
      val attributes = nodesAttributes(nodeKey)
      if (attributes.nonEmpty || withIsolatedNodes) {
        val attributesStr = renderAttributes(attributes)
        Seq(s"""  "$nodeKey" $attributesStr;""")
      } else Seq.empty
    }

    val arcsStrs = for {
      (from, edges) <- nodes
      (to, labels)  <- edges
      label         <- labels
    } yield {
      val attributesStr = renderAttributes(
        RenderAttribute("label", s""""$label"""") +: edgesAttributes(label)
      )
      s"""  "${from}" -> "$to" $attributesStr;"""
    }

    s"""
        |digraph {
        |  overlap=false;
        |  node [shape=box,style=filled];
        |  sep = "+80,80";
        |${nodesStrs.mkString("\n")}
        |${arcsStrs.mkString("\n")}
        |}""".stripMargin
  }
}

object SparseGraph {
  private type InternalRep[NodeTag, EdgeTag] = Map[NodeTag, Map[NodeTag, Set[EdgeTag]]]

  def empty[NodeTag, EdgeTag]: SparseGraph[NodeTag, EdgeTag] =
    new SparseGraph(Map.empty)

  case class RenderAttribute(key: String, value: String)
}
