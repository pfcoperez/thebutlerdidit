package org.pfcoperez.thebutlerdidit.model

import org.pfcoperez.thebutlerdidit.datastructures.SparseGraph
import Report._

case class Report(threads: Seq[ThreadDescription]) {
    def asGraph: SparseGraph[ThreadId, ObjectReference] = {

        def threadId(thread: ThreadDescription): String = {
            s"#${thread.id} ${thread.name}"
        }

        val objectToOwner = {
            for {
                thread <- threads
                ownedObject <- thread.locking
            } yield ownedObject -> threadId(thread)
        }.toMap
        
        threads.foldLeft(SparseGraph.empty[ThreadId, ObjectReference]) {
            case (graph, thread) =>
                val to = threadId(thread)
                thread.lockedBy.foldLeft(graph + to) {
                    case (current, objAddr) =>
                        val from = objectToOwner.getOrElse(objAddr, unknownThreadId)
                        current + (from -> objAddr -> to)
                }
        }
    }
}

object Report {
    type ThreadId = String
    type ObjectReference = BigInt

    val unknownThreadId = "UNKNOWN"
}