package org.pfcoperez.thebutlerdidit

import scalajs.js.Dynamic
import org.scalajs.dom
import org.scalajs.dom.document
import org.scalajs.dom.Element
import org.scalajs.dom.html.{Option => _, Element => _, _}

import jsfacades.Viz
import ThreadDumpParsers.parseReportString

import scala.concurrent.ExecutionContext.Implicits.global

object WebUI {

    def processReport(reportStr: String): String  = {
        val result = parseReportString(reportStr)
        result.fold(
            { case problem => problem.toString },
            { case (report, _) => report.asGraph.renderGraphviz }
        )
    }

    def main(args: Array[String]): Unit = {

        val viz = new Viz()

        val div = document.createElement("div")
        document.body.appendChild(div)

        val inputTextNode = document.createElement("textarea").asInstanceOf[TextArea]
        inputTextNode.cols = 80
        inputTextNode.rows = 32

        val outTextNode = document.createElement("textarea").asInstanceOf[TextArea]
        outTextNode.cols = 80
        outTextNode.rows = 32
        outTextNode.disabled = true

        val button = document.createElement("button").asInstanceOf[Button]
        button.textContent = "Analyze!"

        button.addEventListener("click", { (e: dom.MouseEvent) =>
            val dotReport = processReport(inputTextNode.value)
            outTextNode.textContent = dotReport
            button.disabled = true
            inputTextNode.disabled = true

            val vizOptions = Dynamic.literal(engine = "neato")

            viz.renderSVGElement(dotReport, vizOptions).toFuture.foreach { graphElement =>
                div.appendChild(graphElement)
            }
        })

        div.appendChild(inputTextNode)
        div.appendChild(outTextNode)
        div.appendChild(button)
    }
}
