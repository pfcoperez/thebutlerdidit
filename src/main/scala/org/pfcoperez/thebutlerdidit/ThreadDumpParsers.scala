package org.pfcoperez.thebutlerdidit

import fastparse._
import model.ObjectLockState
import org.pfcoperez.thebutlerdidit.model.ThreadDescription
import org.pfcoperez.thebutlerdidit.model.ThreadDescription.SimplifiedStatus
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockedObject
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockRequest

object ThreadDumpParsers {
  import BasicParsers._

  object BasicParsers {
    import NoWhitespace._

    case class WithUnits[T](number: T, unit: String)

    private def sign[_ : P] = P("+" | "-")

    def digits[_ : P] = P(sign.? ~ CharsWhileIn("0-9", 1)).!
    def intNumber[_ : P] = P(digits).map(_.toInt)
    def floatNumber[_ : P] =
      P((digits ~ "." ~ digits).map { case (i, f) => s"$i.$f".toFloat })

    def floatWithUnits[_ : P](unit: String) =
      P(floatNumber ~ unit.!).map { case (magnitude, unit) =>
        WithUnits(magnitude, unit)
      }

    def hexDec[_ : P] =
      P("0" ~ IgnoreCase("X") ~ CharsWhileIn("abcdefABCDEF0-9", 1).!).map { strDigits =>
        BigInt(strDigits, 16)
      }
  }

  import MultiLineWhitespace._

  object ThreadElementsParsers {

    def threadName[_ : P] = P( "\"" ~ CharsWhile(_ != '"').! ~ "\"")
    def threadNo[_ : P] = P("#" ~ intNumber)
    def priority[_ : P] = P("prio=" ~ intNumber)
    def osPriority[_ : P] = P("os_prio=" ~ intNumber)
    def threadAddress[_ : P] = P("tid=" ~ hexDec)
    def osThreadAddress[_ : P] = P("nid=" ~ hexDec)
    def status[_ : P] =
      P(("runnable" | "waiting on condition" | "waiting for monitor entry").!)
      .map(SimplifiedStatus.factories.apply)
    def stackPointer[_ : P] = P("[" ~ hexDec ~ "]")

    def threadDescription[_ : P] =
      P(threadName ~ threadNo ~ priority ~ osPriority ~ threadAddress ~ osThreadAddress ~ status ~ stackPointer)
        .map {
          case (name, number, priority, osPriority, threadAddress, osThreadAddress, status, stackPointer) =>
            ThreadDescription(name, number, priority, osPriority, threadAddress, osThreadAddress, status, stackPointer)
        }

    def threadState[_ : P] = P(
      "java.lang.Thread.State:" ~ (
        "NEW" | "RUNNABLE" | "BLOCKED" | "WAITING" | "TIMED_WAITING" | "TERMINATED"
        ).!
    ).map(Thread.State.valueOf)

    def stackFrame[_ : P] = P("at" ~ CharsWhile(_ != '(') ~ "(" ~ CharsWhile(_ != ')') ~ ")")

    def lockState[_ : P] = P(
      "-" ~ StringIn("locked", "waiting to lock").! ~ "<" ~ hexDec ~ ">" ~ "(" ~ CharsWhile(_ != ')') ~ ")"
    ).map { case (representation: String, address: BigInt) =>
      ObjectLockState.factories(representation)(address)
    }

    def stackLine[_ : P] = P(stackFrame.!.map(_ => None) | lockState.map(Option.apply))
  }

  import ThreadElementsParsers._

  def thread[_ : P] = P(threadDescription ~ threadState ~ stackLine.rep).map {
    case (thread, state, stackLines) =>
      val lockedBy = stackLines.collect {
        case Some(locked: LockRequest) => locked.address
      }
      val locking = stackLines.collect {
        case Some(locked: LockedObject) => locked.address
      }
      thread.copy(
        lockedBy = lockedBy.toSet,
        locking = locking.toSet
      )
  }

}
