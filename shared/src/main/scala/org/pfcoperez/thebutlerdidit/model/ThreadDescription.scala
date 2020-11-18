package org.pfcoperez.thebutlerdidit.model

import ThreadDescription._
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockedObject
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockRequest

case class ThreadDescription(
    name: String,
    id: Int,
    jvmPriority: Int,
    osPriority: Int,
    maybeCpuTime: Option[WithUnits[Float]],
    maybeWallTime: Option[WithUnits[Float]],
    address: BigInt,
    osAddress: BigInt,
    status: SimplifiedStatus,
    stackPointer: BigInt,
    lockedBy: Set[BigInt] = Set.empty,
    ownedInstances: Map[BigInt, String] = Map.empty
) {
  def locking: Set[BigInt] = ownedInstances.keySet
}

object ThreadDescription {
  sealed trait SimplifiedStatus {
    def representation: String
  }

  object SimplifiedStatus {
    def factories(rep: String): SimplifiedStatus = {
      val static = Seq(
        Runnable,
        WaitingForMonitor,
        WaitingOnCondition,
        Sleeping
      ).map(x => x.representation -> x).toMap

      val tag = if (rep.startsWith("in")) WaitingForMonitor.representation else rep

      static(tag)
    }
  }

  case object Runnable extends SimplifiedStatus {
    def representation: String = "runnable"
  }
  case object WaitingOnCondition extends SimplifiedStatus {
    def representation: String = "waiting on condition"
  }
  case object WaitingForMonitor extends SimplifiedStatus {
    def representation: String = "waiting for monitor entry"
  }
  case object Sleeping extends SimplifiedStatus {
    def representation: String = "sleeping"
  }

}
