package org.pfcoperez.thebutlerdidit

import org.scalatest.Inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import fastparse._

import org.pfcoperez.thebutlerdidit.model._

class ThreadDumpParsersSpec extends AnyFunSpec with Matchers with Inside {
  import ThreadDumpParsersSpec._

  describe("ThreadElementsParsers") {
    import ThreadDumpParsers.ThreadElementsParsers._
    import ThreadDumpParsers.{ deadLockElements, thread }

    it("should parse thread description lines") {
      import SingleLineWhitespace._

      val threadDescriptionLine = aThread.lines.toSeq.dropWhile(_ == "").head

      inside(parse(threadDescriptionLine, threadDescription(_))) { case Parsed.Success(x, _) =>
      }

      val wrongLines = Seq(
        """"main" #1 prio=badf00d os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]""",
        """"main" #1 os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]""",
        """"main" #1 prio=5 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]""",
        """"main" #1 prio=5 os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000"""
      )

      wrongLines.foreach { line =>
        withClue(s" malformed line: $line") {
          val result = parse(line, threadDescription(_))
          assert(!result.isSuccess)
        }
      }

    }

    it("should parse all the details for a given thread") {
      import MultiLineWhitespace._

      val result = parse(aThread, thread(_))

      inside(result) { case Parsed.Success(x, _) =>
        x.lockedBy.size shouldBe 1
        x.locking.size shouldBe 3
      }
    }

    it("should parse dead locks sections") {
      import MultiLineWhitespace._

      val result = parse(aDeadLocksSection, deadLockElements(_))

      inside(result) { case Parsed.Success(foundLocks, _) =>
        foundLocks.size shouldBe 6
      }
    }

  }

}

object ThreadDumpParsersSpec {

  val aDeadLocksSection =
    """
      |Found one Java-level deadlock:
      |=============================
      |"MissScarlett$":
      |  waiting to lock monitor 0x00007f77e800a0e8 (object 0x00000000f6000e58, a org.pfcoperez.thebutlerdidit.SampleApp$Revolver$),
      |  which is held by "ProfessorPlum$"
      |"ProfessorPlum$":
      |  waiting to lock monitor 0x00007f77b40036a8 (object 0x00000000f61024b8, a org.pfcoperez.thebutlerdidit.SampleApp$LeadPipe$),
      |  which is held by "ColonelMustard$"
      |"ColonelMustard$":
      |  waiting to lock monitor 0x00007f77e000d3a8 (object 0x00000000f609df68, a org.pfcoperez.thebutlerdidit.SampleApp$Dagger$),
      |  which is held by "RevGreen$"
      |"RevGreen$":
      |  waiting to lock monitor 0x00007f77e000afe8 (object 0x00000000f60c7e38, a org.pfcoperez.thebutlerdidit.SampleApp$Candlestick$),
      |  which is held by "MissScarlett$"
      |
      |Java stack information for the threads listed above:
      |===================================================
      |"MissScarlett$":
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:29)
      |	- waiting to lock <0x00000000f6000e58> (a org.pfcoperez.thebutlerdidit.SampleApp$Revolver$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MissScarlett$$anonfun$$lessinit$greater$1.$anonfun$apply$1(SampleApp.scala:51)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MissScarlett$$anonfun$$lessinit$greater$1$$Lambda$5931/691456804.apply$mcV$sp(Unknown Source)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:30)
      |	- locked <0x00000000f60c7e38> (a org.pfcoperez.thebutlerdidit.SampleApp$Candlestick$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MissScarlett$$anonfun$$lessinit$greater$1.apply$mcV$sp(SampleApp.scala:50)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$Suspect.run(SampleApp.scala:36)
      |"ProfessorPlum$":
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:29)
      |	- waiting to lock <0x00000000f61024b8> (a org.pfcoperez.thebutlerdidit.SampleApp$LeadPipe$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$ProfessorPlum$$anonfun$$lessinit$greater$4.$anonfun$apply$7(SampleApp.scala:86)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$ProfessorPlum$$anonfun$$lessinit$greater$4$$Lambda$5928/198355539.apply$mcV$sp(Unknown Source)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:30)
      |	- locked <0x00000000f6000e58> (a org.pfcoperez.thebutlerdidit.SampleApp$Revolver$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$ProfessorPlum$$anonfun$$lessinit$greater$4.apply$mcV$sp(SampleApp.scala:82)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$Suspect.run(SampleApp.scala:36)
      |"ColonelMustard$":
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:29)
      |	- waiting to lock <0x00000000f609df68> (a org.pfcoperez.thebutlerdidit.SampleApp$Dagger$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$ColonelMustard$$anonfun$$lessinit$greater$3.$anonfun$apply$5(SampleApp.scala:74)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$ColonelMustard$$anonfun$$lessinit$greater$3$$Lambda$5937/1438776930.apply$mcV$sp(Unknown Source)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:30)
      |	- locked <0x00000000f61024b8> (a org.pfcoperez.thebutlerdidit.SampleApp$LeadPipe$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$ColonelMustard$$anonfun$$lessinit$greater$3.apply$mcV$sp(SampleApp.scala:73)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$Suspect.run(SampleApp.scala:36)
      |"RevGreen$":
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:29)
      |	- waiting to lock <0x00000000f60c7e38> (a org.pfcoperez.thebutlerdidit.SampleApp$Candlestick$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$RevGreen$$anonfun$$lessinit$greater$2.$anonfun$apply$3(SampleApp.scala:62)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$RevGreen$$anonfun$$lessinit$greater$2$$Lambda$5938/791771374.apply$mcV$sp(Unknown Source)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:30)
      |	- locked <0x00000000f609df68> (a org.pfcoperez.thebutlerdidit.SampleApp$Dagger$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$RevGreen$$anonfun$$lessinit$greater$2.apply$mcV$sp(SampleApp.scala:61)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$Suspect.run(SampleApp.scala:36)
      |
      |Found one Java-level deadlock:
      |=============================
      |"MrsWhite$":
      |  waiting to lock monitor 0x00007f78e6c60508 (object 0x00000000f5f9b260, a org.pfcoperez.thebutlerdidit.SampleApp$Rope$),
      |  which is held by "MrsPeakcock$"
      |"MrsPeakcock$":
      |  waiting to lock monitor 0x00007f77e80084b8 (object 0x00000000f6066928, a org.pfcoperez.thebutlerdidit.SampleApp$Wrench$),
      |  which is held by "MrsWhite$"
      |
      |Java stack information for the threads listed above:
      |===================================================
      |"MrsWhite$":
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:29)
      |	- waiting to lock <0x00000000f5f9b260> (a org.pfcoperez.thebutlerdidit.SampleApp$Rope$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MrsWhite$$anonfun$$lessinit$greater$6.$anonfun$apply$11(SampleApp.scala:106)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MrsWhite$$anonfun$$lessinit$greater$6$$Lambda$5929/1502138185.apply$mcV$sp(Unknown Source)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:30)
      |	- locked <0x00000000f6066928> (a org.pfcoperez.thebutlerdidit.SampleApp$Wrench$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MrsWhite$$anonfun$$lessinit$greater$6.apply$mcV$sp(SampleApp.scala:105)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$Suspect.run(SampleApp.scala:36)
      |"MrsPeakcock$":
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:29)
      |	- waiting to lock <0x00000000f6066928> (a org.pfcoperez.thebutlerdidit.SampleApp$Wrench$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MrsPeakcock$$anonfun$$lessinit$greater$5.$anonfun$apply$9(SampleApp.scala:97)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MrsPeakcock$$anonfun$$lessinit$greater$5$$Lambda$5926/881916961.apply$mcV$sp(Unknown Source)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$.grabbingItem(SampleApp.scala:30)
      |	- locked <0x00000000f5f9b260> (a org.pfcoperez.thebutlerdidit.SampleApp$Rope$)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$MrsPeakcock$$anonfun$$lessinit$greater$5.apply$mcV$sp(SampleApp.scala:93)
      |	at org.pfcoperez.thebutlerdidit.SampleApp$Suspect.run(SampleApp.scala:36)
    """.stripMargin.trim

  val aThread =
    """
      |"main" #1 prio=5 os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]
      |   java.lang.Thread.State: RUNNABLE
      |    at java.io.FileInputStream.readBytes(Native Method)
      |    at java.io.FileInputStream.read(FileInputStream.java:255)
      |    at java.io.BufferedInputStream.read1(BufferedInputStream.java:284)
      |    at java.io.BufferedInputStream.read(BufferedInputStream.java:345)
      |    - locked <0x00000000e1003d40> (a java.io.BufferedInputStream)
      |    at sun.nio.cs.StreamDecoder.readBytes(StreamDecoder.java:284)
      |    at sun.nio.cs.StreamDecoder.implRead(StreamDecoder.java:326)
      |    at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:178)
      |    - locked <0x00000000e1003df8> (a java.io.InputStreamReader)
      |    at sun.nio.cs.StreamDecoder.read0(StreamDecoder.java:127)
      |    - locked <0x00000000e1003df8> (a java.io.InputStreamReader)
      |    at sun.nio.cs.StreamDecoder.read(StreamDecoder.java:112)
      |    at java.io.InputStreamReader.read(InputStreamReader.java:168)
      |    at ammonite.terminal.Terminal$.$anonfun$readLine$3(Terminal.scala:41)
      |    at ammonite.terminal.Terminal$$$Lambda$376/1833848849.apply$mcI$sp(Unknown Source)
      |    at scala.runtime.java8.JFunction0$mcI$sp.apply(JFunction0$mcI$sp.java:23)
      |    at ammonite.terminal.LazyList.head$lzycompute(Utils.scala:142)
      |    - locked <0x00000000e1003e20> (a ammonite.terminal.LazyList)
      |    at ammonite.terminal.LazyList.head(Utils.scala:140)
      |    at ammonite.terminal.LazyList$$tilde$colon$.unapply(Utils.scala:167)
      |    at ammonite.terminal.filters.UndoFilter.$anonfun$filter$1(UndoFilter.scala:126)
      |    at ammonite.terminal.filters.UndoFilter$$Lambda$412/1678046232.apply(Unknown Source)
      |    at ammonite.terminal.Filter$$anon$4.op(Filter.scala:83)
      |    at ammonite.terminal.Filter$$anon$5.$anonfun$op$7(Filter.scala:93)
      |    at ammonite.terminal.Filter$$anon$5$$Lambda$410/268084911.apply(Unknown Source)
      |    at scala.collection.Iterator$$anon$10.next(Iterator.scala:461)
      |    at scala.collection.Iterator.find(Iterator.scala:994)
      |    at scala.collection.Iterator.find$(Iterator.scala:992)
      |    at scala.collection.AbstractIterator.find(Iterator.scala:1431)
      |    at ammonite.terminal.Filter$$anon$5.op(Filter.scala:93)
      |    at ammonite.terminal.DelegateFilter.op(Filter.scala:109)
      |    at ammonite.terminal.Filter$$anon$5.$anonfun$op$7(Filter.scala:93)
      |    at ammonite.terminal.Filter$$anon$5$$Lambda$410/268084911.apply(Unknown Source)
      |    at scala.collection.Iterator$$anon$10.next(Iterator.scala:461)
      |    at scala.collection.Iterator.find(Iterator.scala:994)
      |    at scala.collection.Iterator.find$(Iterator.scala:992)
      |    at scala.collection.AbstractIterator.find(Iterator.scala:1431)
      |    at ammonite.terminal.Filter$$anon$5.op(Filter.scala:93)
      |    at ammonite.terminal.LineReader.readChar(LineReader.scala:157)
      |    at ammonite.terminal.Terminal$.$anonfun$readLine$2(Terminal.scala:41)
      |    at ammonite.terminal.Terminal$$$Lambda$309/737945227.apply(Unknown Source)
      |    at ammonite.terminal.TTY$.withSttyOverride(Utils.scala:117)
      |    - waiting to lock <0x0000000780a000b0> (a com.nbp.theplatform.threaddump.ThreadBlockedState)            
      |    at ammonite.terminal.Terminal$.readLine(Terminal.scala:41)
      |    at ammonite.repl.AmmoniteFrontEnd.readLine(AmmoniteFrontEnd.scala:133)
      |    at ammonite.repl.AmmoniteFrontEnd.action(AmmoniteFrontEnd.scala:25)
      |    at ammonite.repl.Repl.$anonfun$action$4(Repl.scala:192)
      |    at ammonite.repl.Repl$$Lambda$178/807752428.apply(Unknown Source)
      |    at ammonite.repl.Scoped.$anonfun$flatMap$1(Signaller.scala:45)
      |    at ammonite.repl.Scoped$$Lambda$179/1073763441.apply(Unknown Source)
      |    at ammonite.repl.Signaller.apply(Signaller.scala:28)
      |    at ammonite.repl.Scoped.flatMap(Signaller.scala:45)
      |    at ammonite.repl.Scoped.flatMap$(Signaller.scala:45)
      |    at ammonite.repl.Signaller.flatMap(Signaller.scala:16)
      |    at ammonite.repl.Repl.$anonfun$action$2(Repl.scala:176)
      |    at ammonite.repl.Repl$$Lambda$176/2030538903.apply(Unknown Source)
      |    at ammonite.util.Catching.flatMap(Res.scala:115)
      |    at ammonite.repl.Repl.action(Repl.scala:168)
      |    at ammonite.repl.Repl.loop$1(Repl.scala:208)
      |    at ammonite.repl.Repl.run(Repl.scala:223)
      |    at ammonite.Main.$anonfun$run$1(Main.scala:223)
      |    at ammonite.Main$$Lambda$174/442987331.apply(Unknown Source)
      |    at scala.Option.getOrElse(Option.scala:189)
      |    at ammonite.Main.run(Main.scala:211)
      |    at ammonite.MainRunner.$anonfun$runRepl$1(Main.scala:413)
      |    at ammonite.MainRunner$$Lambda$101/1337344609.apply(Unknown Source)
      |    at ammonite.MainRunner.watchLoop(Main.scala:394)
      |    at ammonite.MainRunner.runRepl(Main.scala:413)
      |    at ammonite.Main$.main0(Main.scala:320)
      |    at ammonite.Main$.main(Main.scala:270)
      |    at ammonite.Main.main(Main.scala)
      |
      |   Locked ownable synchronizers:
      |    - None
      |""".stripMargin.trim()

}
