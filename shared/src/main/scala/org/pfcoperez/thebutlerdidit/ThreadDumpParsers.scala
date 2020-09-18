package org.pfcoperez.thebutlerdidit

import fastparse._
import model.ObjectLockState
import org.pfcoperez.thebutlerdidit.model.ThreadDescription
import org.pfcoperez.thebutlerdidit.model.ThreadDescription.SimplifiedStatus
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockedObject
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockRequest
import org.pfcoperez.thebutlerdidit.model.Report

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

    def parameters[_ : P] = P("(" ~ CharsWhile(_ != ')', 0) ~ ")").!

    def method[_ : P] = P(CharsWhile(_ != '(') ~ parameters).!

    def status[_ : P] =
      P(("runnable" | "waiting on condition" | "waiting for monitor entry" | "in" ~ method | "sleeping").!)
      .map(SimplifiedStatus.factories)
    def stackPointer[_ : P] = P("[" ~ hexDec ~ "]")

    def threadDescription[_ : P] =
      P(threadName ~ threadNo ~ "daemon".? ~ priority ~ osPriority ~ threadAddress ~ osThreadAddress ~ status ~ stackPointer)
        .map {
          case (name, number, priority, osPriority, threadAddress, osThreadAddress, status, stackPointer) =>
            ThreadDescription(name, number, priority, osPriority, threadAddress, osThreadAddress, status, stackPointer)
        }

    def threadState[_ : P] = P(
      "java.lang.Thread.State:" ~ (
        "NEW" | "RUNNABLE" | "BLOCKED" | "WAITING" | "TIMED_WAITING" | "TERMINATED"
        ).! ~ parameters.?
    ).map(_._1)

    def stackFrame[_ : P] = P("at" ~ method)

    def addressAndClass[_ : P] = P("<" ~ hexDec ~ ">" ~ "(" ~ "a" ~ CharsWhile(_ != ')').! ~ ")")

    def lockState[_ : P] = P(
      "-" ~ StringIn(
        "locked",
        "waiting to lock",
        "waiting to re-lock in wait()",
        "waiting on",
        "parking to wait for"
      ).! ~ addressAndClass
    ).map { case (representation: String, (address: BigInt, className: String)) =>
      ObjectLockState.factories(representation)(address, className)
    }

    def stackLine[_ : P] = P((stackFrame | "- None" | "No compile task").!.map(_ => None) | lockState.map(Option.apply))

    def lockedOwnableSyncs[_ : P] = P("Locked ownable synchronizers:" ~ (stackLine | ("-" ~ addressAndClass)).rep)
  }

  import ThreadElementsParsers._

  def thread[_ : P] = P(threadDescription ~ threadState ~ stackLine.rep ~ lockedOwnableSyncs).map {
    case (thread, _, stackLines, _) =>
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

  def header[_ : P] = P(CharsWhile(_ != '"'))

  def jvmThread[_ : P] = P(threadName ~ osPriority ~ threadAddress ~ osThreadAddress ~ status) 

  def threads[_ : P] = P(thread.rep ~ jvmThread.rep).map(_._1)

  def jni[_ : P] = P("JNI global references:" ~ intNumber)

  def unusedTail[_ : P] = P(AnyChar.rep ~ End)

  def report[_ : P] = P(header ~ threads ~ jvmThread.rep ~ jni ~ unusedTail.? ~ End).map { t =>
    Report(t._1)
  }

  def parseReportString(str: String): Parsed[Report] = parse(str, report(_))
}
