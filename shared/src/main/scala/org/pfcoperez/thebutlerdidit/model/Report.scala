package org.pfcoperez.thebutlerdidit.model

import org.pfcoperez.thebutlerdidit.datastructures.SparseGraph
import Report._

case class Report(threads: Seq[ThreadDescription], deadLockElements: Set[DeadLockElement]) {

  // Method replacing `x.formatted("%x")` which seems to be buggy in Scala.js
  private def bigIntToHexStr(x: BigInt): String = {
    val digitChar = (10 to 15).zip('a' to 'z').toMap
    def digits(x: BigInt, acc: List[Int]): List[Int] =
      if (x == 0) acc
      else digits(x / 16, (x % 16).toInt :: acc)
    "0x" + digits(x, Nil).map(d => s"${digitChar.getOrElse(d, d)}").mkString
  }

  def asGraph: SparseGraph[ThreadId, ObjectReference] = {

    def threadId(thread: ThreadDescription): String = thread.name

    val objectToOwner = {
      for {
        thread      <- threads
        ownedObject <- thread.locking
      } yield ownedObject -> threadId(thread)
    }.toMap

    threads.foldLeft(SparseGraph.empty[ThreadId, ObjectReference]) {
      case (graph, thread) =>
        val to = threadId(thread)
        thread.lockedBy.foldLeft(graph + to) {
          case (current, objAddr) =>
            val hexStr = bigIntToHexStr(objAddr)
            objectToOwner.get(objAddr).map { from =>
              current + (from -> hexStr -> to)
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
