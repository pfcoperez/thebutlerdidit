package org.pfcoperez.thebutlerdidit

import ThreadDumpParsers.parseReportString
import scala.io.Source


object ProcessInput extends App {

    val inputStr =
        Source.fromInputStream(System.in).getLines().mkString("\n")

    val parsedResult = parseReportString(inputStr)

    parsedResult.fold(
        { case problem => println(problem) },
        {
            case (report, _) =>
                println(report.asGraph)
        }
    )

}