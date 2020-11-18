package org.pfcoperez.thebutlerdidit

import org.scalatest.Inside
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers
import fastparse._
import java.io.File

import org.pfcoperez.thebutlerdidit.model._
import java.nio.file.Path
import java.nio.file.Paths

class ThreadDumpParsersJVMSpec extends AnyFunSpec with Matchers with Inside {

  describe("ThreadElementsParsers (JVM)") {
    import ThreadDumpParsers.ThreadElementsParsers._
    import ThreadDumpParsers.thread

    it("should parse complete reports") {
      import MultiLineWhitespace._
      import ThreadDumpParsers.report

      val resource = Thread.currentThread().getContextClassLoader().getResource("samples/sample01")

      val samplesDir = new File(
        resource.getPath()
      ).getParentFile()

      samplesDir.listFiles().foreach { sampleFile =>
        withClue(s" Trying sample: ${sampleFile.getName()} ") {
          val contentsStr = scala.io.Source.fromFile(sampleFile)(scala.io.Codec.UTF8).getLines().mkString("\n")

          val result = parse(contentsStr, report(_))

          inside(result) { case Parsed.Success(Report(threads, _, references), _) =>
            assert(threads.size > 1)
            assert(references.size > 0)
          }
        }
      }

    }

  }

}
