package org.pfcoperez.thebutlerdidit.datastructures

import SparseGraph._

class SparseGraph[NodeTag, EdgeTag] private (
    private val nodes: InternalRep[NodeTag, EdgeTag] = Map.empty
) {

    def getNodes: Set[NodeTag] = {
        nodes.keySet
    }

    def getEdges(from: NodeTag, to: NodeTag): Set[EdgeTag] = {
        getEdges(from).getOrElse(to, Set.empty)
    }

    def getEdges(from: NodeTag): Map[NodeTag, Set[EdgeTag]] = {
        nodes.getOrElse(from, Map.empty)
    }

    def +(node: NodeTag): SparseGraph[NodeTag, EdgeTag] = {
        nodes.get(node).map(_ => this).getOrElse {
            new SparseGraph(nodes + (node -> Map.empty))
        }
    }

    def addEdge(
        from: NodeTag,
        to: NodeTag,
        tag: EdgeTag
    ): SparseGraph[NodeTag, EdgeTag] = {
        val currentFromEdges = nodes.getOrElse(from, Map.empty) 
        val currentEdges = currentFromEdges.getOrElse(to, Set.empty)
        new SparseGraph(
            nodes + (
                from -> (currentFromEdges + (
                    to -> (currentEdges + tag)
                    )
                )
            )
        )
    }

    def +(edge: ((NodeTag, EdgeTag), NodeTag)): SparseGraph[NodeTag, EdgeTag] = {
        val ((from, tag), to) = edge
        addEdge(from, to, tag)
    }
}

object SparseGraph {
    private type InternalRep[NodeTag, EdgeTag] = Map[NodeTag, Map[NodeTag, Set[EdgeTag]]]
}
