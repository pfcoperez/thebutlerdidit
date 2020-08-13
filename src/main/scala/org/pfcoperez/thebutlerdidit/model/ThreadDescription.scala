package org.pfcoperez.thebutlerdidit.model

import ThreadDescription._

case class ThreadDescription(
    name: String,
    id: Int,
    jvmPriority: Int,
    osPriority: Int,
    address: BigInt,
    osAddress: BigInt,
    status: SimplifiedStatus,
    stackPointer: BigInt
)

object ThreadDescription {
    sealed trait SimplifiedStatus {
        def representation: String
    }

    object SimplifiedStatus {
        val factories: Map[String, SimplifiedStatus] = Seq(
            Runnable, WaitingForMonitor, WaitingOnCondition
        ).map(x => x.representation -> x).toMap
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