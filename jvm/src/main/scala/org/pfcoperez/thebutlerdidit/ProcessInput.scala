package org.pfcoperez.thebutlerdidit

import org.pfcoperez.thebutlerdidit.ThreadDumpParsers.parseReportString
import scala.io.Source

object ProcessInput extends App {

  val inputStr =
    Source.fromInputStream(System.in).getLines().mkString("\n")

  val parsedResult = parseReportString(inputStr)

  parsedResult.fold(
    { case problem => println(problem._3.trace().msg) },
    { case (report, _) =>
      println(report.asGraph.renderGraphviz(true))
    }
  )

}
