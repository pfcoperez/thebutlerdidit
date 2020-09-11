package org.pfcoperez.thebutlerdidit.model

import org.pfcoperez.thebutlerdidit.datastructures.SparseGraph
import Report._

case class Report(threads: Seq[ThreadDescription]) {
    def asGraph: SparseGraph[ThreadId, ObjectReference] = {

        val objectToOwner = {
            for {
                thread <- threads
                ownedObject <- thread.locking
            } yield ownedObject -> thread.id
        }.toMap
        
        threads.foldLeft(SparseGraph.empty[ThreadId, ObjectReference]) {
            case (graph, thread) =>
                thread.lockedBy.foldLeft(graph) {
                    case (current, objAddr) =>
                        val from = objectToOwner.getOrElse(objAddr, unknownThreadId)
                        val to = thread.id
                        current + (from -> objAddr -> to)
                }
        }
    }
}

object Report {
    type ThreadId = Int
    type ObjectReference = BigInt

    val unknownThreadId = -1
}