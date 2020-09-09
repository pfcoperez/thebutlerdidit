package org.pfcoperez.thebutlerdidit

import org.scalatest.Inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import fastparse._
import java.io.File

import org.pfcoperez.thebutlerdidit.model._

class ThreadDumpParsersSpec extends AnyFunSpec with Matchers with Inside {
  import ThreadDumpParsersSpec._

  describe("ThreadElementsParsers") {
    import ThreadDumpParsers.ThreadElementsParsers._
    import ThreadDumpParsers.thread

    it("should parse thread description lines") {
      import SingleLineWhitespace._

      val threadDescriptionLine = aThread.lines.toSeq.dropWhile(_ == "").head

      inside(parse(threadDescriptionLine, threadDescription(_))) {
        case Parsed.Success(x, _) =>
      }

      val wrongLines = Seq(
        """"main" #1 prio=badf00d os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]""",
        """"main" #1 os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]""",
        """"main" #1 prio=5 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000]""",
        """"main" #1 prio=5 os_prio=0 tid=0x00007fb54004d000 nid=0x1c3d runnable [0x00007fb546c21000""",
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
      
      inside(result) {
        case Parsed.Success(x, _) =>
          x.lockedBy.size shouldBe 1
          x.locking.size shouldBe 3
      }
    }

    it("should parse complete reports") {
      import MultiLineWhitespace._
      import ThreadDumpParsers.report

      val contentsStr = scala.io.Source.fromFile(
        new File(Thread.currentThread().getContextClassLoader().getResource("sample01").getPath())
      )(scala.io.Codec.UTF8).getLines().mkString("\n")

      val result = parse(contentsStr, report(_))

      inside(result) {
        case Parsed.Success(Report(threads), _) =>
          threads.foreach(println)
          assert(threads.size > 1)
        //case f @ Parsed.Failure(label, index, _) =>
        //  println(s"$f $label $index")
      }

    }

  }

}

object ThreadDumpParsersSpec {

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
