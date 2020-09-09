package org.pfcoperez.thebutlerdidit.model

import ThreadDescription._
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockedObject
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockRequest

case class ThreadDescription(
    name: String,
    id: Int,
    jvmPriority: Int,
    osPriority: Int,
    address: BigInt,
    osAddress: BigInt,
    status: SimplifiedStatus,
    stackPointer: BigInt,
    lockedBy: Set[BigInt] = Set.empty,
    locking: Set[BigInt] = Set.empty
)

object ThreadDescription {
    sealed trait SimplifiedStatus {
        def representation: String
    }

    object SimplifiedStatus {
        def factories(rep: String): SimplifiedStatus = {
            val static = Seq(
                Runnable, WaitingForMonitor, WaitingOnCondition
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
}