package org.pfcoperez.thebutlerdidit

import fastparse._
import model.ObjectLockState
import org.pfcoperez.thebutlerdidit.model.ThreadDescription
import org.pfcoperez.thebutlerdidit.model.ThreadDescription.SimplifiedStatus
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockedObject
import org.pfcoperez.thebutlerdidit.model.ObjectLockState.LockRequest
import org.pfcoperez.thebutlerdidit.model.Report
import org.pfcoperez.thebutlerdidit.model.WithUnits

object ThreadDumpParsers {
  import BasicParsers._

  object BasicParsers {
    import NoWhitespace._

    private def sign[_: P] = P("+" | "-")

    def digits[_: P]    = P(sign.? ~ CharsWhileIn("0-9", 1)).!
    def intNumber[_: P] = P(digits).map(_.toInt)
    def floatNumber[_: P] =
      P((digits ~ "." ~ digits).map { case (i, f) => s"$i.$f".toFloat })

    def floatWithUnits[_: P] =
      P(floatNumber ~ CharsWhileIn("a-zA-Z/*").!).map {
        case (magnitude, unit) =>
          WithUnits(magnitude, unit)
      }

    def hexDec[_: P] =
      P("0" ~ IgnoreCase("X") ~ CharsWhileIn("abcdefABCDEF0-9", 1).!).map { strDigits =>
        BigInt(strDigits, 16)
      }
  }

  import MultiLineWhitespace._

  object ThreadElementsParsers {

    def threadName[_: P]      = P("\"" ~ CharsWhile(_ != '"').! ~ "\"")
    def threadNo[_: P]        = P("#" ~ intNumber)
    def priority[_: P]        = P("prio=" ~ intNumber)
    def osPriority[_: P]      = P("os_prio=" ~ intNumber)
    def cpuTime[_: P]         = P("cpu=" ~ floatWithUnits)
    def wallTime[_: P]        = P("elapsed=" ~ floatWithUnits)
    def threadAddress[_: P]   = P("tid=" ~ hexDec)
    def osThreadAddress[_: P] = P("nid=" ~ hexDec)

    def parameters[_: P] = P("(" ~ CharsWhile(_ != ')', 0) ~ ")").!

    def method[_: P] = P(CharsWhile(_ != '(') ~ parameters).!

    def status[_: P] =
      P(("runnable" | "waiting on condition" | "waiting for monitor entry" | "in" ~ method | "sleeping").!)
        .map(SimplifiedStatus.factories)
    def stackPointer[_: P] = P("[" ~ hexDec ~ "]")

    def threadDescription[_: P] =
      P(
        threadName ~ threadNo ~ "daemon".? ~ priority ~ osPriority ~ cpuTime.? ~ wallTime.? ~ threadAddress ~ osThreadAddress ~ status ~ stackPointer
      )
        .map {
          case (
                name,
                number,
                priority,
                osPriority,
                maybeCpuTime,
                maybeWallTime,
                threadAddress,
                osThreadAddress,
                status,
                stackPointer
              ) =>
            ThreadDescription(
              name,
              number,
              priority,
              osPriority,
              maybeCpuTime,
              maybeWallTime,
              threadAddress,
              osThreadAddress,
              status,
              stackPointer
            )
        }

    def threadState[_: P] =
      P(
        "java.lang.Thread.State:" ~ (
              "NEW" | "RUNNABLE" | "BLOCKED" | "WAITING" | "TIMED_WAITING" | "TERMINATED"
            ).! ~ parameters.?
      ).map(_._1)

    def stackFrame[_: P] = P("at" ~ method)

    def unknownAddress[_: P] = P("no object reference available").map(_ => Option.empty[BigInt])

    def addressAndClass[_: P] =
      P("<" ~ (hexDec.map(Some(_)) | unknownAddress) ~ ">" ~ ("(" ~ "a" ~ CharsWhile(_ != ')').! ~ ")").?)

    def lockState[_: P] =
      P(
        "-" ~ StringIn(
              "locked",
              "waiting to lock",
              "waiting to re-lock in wait()",
              "waiting on",
              "parking to wait for"
            ).! ~ addressAndClass
      ).map {
        case (representation: String, (maybeAddress: Option[BigInt], maybeClassName: Option[String])) =>
          for {
            address   <- maybeAddress
            className <- maybeClassName
          } yield ObjectLockState.factories(representation)(address, className)
      }

    def stackLine[_: P] = P((stackFrame | "- None" | "No compile task").!.map(_ => None) | lockState)

    def lockedOwnableSyncs[_: P] = P("Locked ownable synchronizers:" ~ (stackLine | ("-" ~ addressAndClass)).rep)
  }

  import ThreadElementsParsers._

  def thread[_: P] =
    P(threadDescription ~ threadState ~ stackLine.rep ~ lockedOwnableSyncs).map {
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

  def header[_: P] = P(CharsWhile(_ != '"'))

  def jvmThread[_: P] = P(threadName ~ osPriority ~ cpuTime.? ~ wallTime.? ~ threadAddress ~ osThreadAddress ~ status)

  def threads[_: P] = P(thread.rep ~ jvmThread.rep).map(_._1)

  def references[_: P](kind: String) = P(kind ~ StringIn("references", "refs") ~ ":" ~ intNumber)
  def jni[_: P]                      = P("JNI" ~ (references("global") | references("weak")))

  def unusedTail[_: P] = P(AnyChar.rep ~ End)

  def report[_: P] =
    P(header ~ threads ~ jvmThread.rep ~ jni ~ unusedTail.? ~ End).map { t =>
      Report(t._1)
    }

  def parseReportString(str: String): Parsed[Report] = parse(str, report(_))
}
