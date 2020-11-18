package org.pfcoperez.thebutlerdidit.model

import org.pfcoperez.thebutlerdidit.datastructures.SparseGraph
import Report._

case class Report(
    threads: Seq[ThreadDescription],
    deadLockElements: Set[DeadLockElement],
    referenceToClass: Map[BigInt, String]
) {

  def asGraph: SparseGraph[ThreadId, ObjectReference] = {

    def threadId(thread: ThreadDescription): String = thread.name

    val objectToOwner = {
      for {
        thread      <- threads
        ownedObject <- thread.locking
      } yield ownedObject -> threadId(thread)
    }.toMap

    threads.foldLeft(SparseGraph.empty[ThreadId, ObjectReference]) { case (graph, thread) =>
      val to = threadId(thread)
      thread.lockedBy.foldLeft(graph + to) { case (current, objAddr) =>
        objectToOwner.get(objAddr).map { from =>
          current + (from -> objAddr.formatted("%#x") -> to)
        } getOrElse (current)

      }
    }
  }
}

object Report {
  type ThreadId        = String
  type ObjectReference = String

  val unknownThreadId = "UNKNOWN"
}
