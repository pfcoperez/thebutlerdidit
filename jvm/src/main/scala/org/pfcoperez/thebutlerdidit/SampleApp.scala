package org.pfcoperez.thebutlerdidit

import scala.language.postfixOps
import scala.concurrent.duration._

object SampleApp extends App {

  println("Starting Mystery Game")

  sealed trait Item {
    def name: String = this.getClass().getSimpleName()
  }

  object Candlestick extends Item
  object Dagger      extends Item
  object LeadPipe    extends Item
  object Revolver    extends Item
  object Rope        extends Item
  object Wrench      extends Item

  def waitFor(duration: FiniteDuration): Unit =
    Thread.sleep(duration.toMillis)

  def grabbingItem(item: Item)(f: => Unit): Unit = {
    val name = Thread.currentThread().getName()

    println(s"[$name] wants [${item.name}] in ${}")
    item.synchronized {
      println(s"[$name] took [${item.name} - ${item}]")
      f
      println(s"[$name] freed [${item.name}]")
    }
  }

  abstract class Suspect(actions: => Unit) extends Thread {
    override def run = actions

    def name: String = this.getClass().getSimpleName()

    def wakeUp: Unit = {
      println(s"Starting [$name]")
      setName(name)
      start()
    }
  }

  object MissScarlett
      extends Suspect(
        grabbingItem(Candlestick) {
          grabbingItem(Revolver) {
            waitFor(1 hours)
          }
        }
      )

  object RevGreen
      extends Suspect({
        MissScarlett.wakeUp
        waitFor(500 milliseconds)
        grabbingItem(Dagger) {
          grabbingItem(Candlestick) {
            waitFor(1 hours)
          }
        }
      })

  object ColonelMustard
      extends Suspect(
        {
          RevGreen.wakeUp
          waitFor(500 milliseconds)
          grabbingItem(LeadPipe) {
            grabbingItem(Dagger) {
              waitFor(1 hours)
            }
          }
        }
      )

  object ProfessorPlum
      extends Suspect(
        grabbingItem(Revolver) {
          ColonelMustard.wakeUp
          waitFor(500 milliseconds)
          grabbingItem(LeadPipe) {
            waitFor(1 hours)
          }
        }
      )

  object MrsPeakcock
      extends Suspect(
        grabbingItem(Rope) {
          MrsWhite.wakeUp
          waitFor(500 milliseconds)
          grabbingItem(Wrench) {
            waitFor(1 hours)
          }
        }
      )

  object MrsWhite
      extends Suspect(
        grabbingItem(Wrench) {
          grabbingItem(Rope) {
            waitFor(1 hour)
          }
        }
      )

  println("...")

  MrsPeakcock.wakeUp
  ProfessorPlum.wakeUp
}
