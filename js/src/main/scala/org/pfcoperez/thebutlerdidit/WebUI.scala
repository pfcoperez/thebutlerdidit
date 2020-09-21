package org.pfcoperez.thebutlerdidit

import scalajs.js.Dynamic
import org.scalajs.dom
import org.scalajs.dom.{ document, window }
import org.scalajs.dom.Element
import org.scalajs.dom.html.{ Option => _, Element => _, _ }

import jsfacades.Viz
import ThreadDumpParsers.parseReportString

import scala.concurrent.ExecutionContext.Implicits.global

object WebUI {

  def processReport(reportStr: String, withIsolatedNodes: Boolean): Either[String, String] = {
    val result = parseReportString(reportStr)
    result.fold(
      { case problem => Left(problem._3.trace().msg) },
      { case (report, _) => Right(report.asGraph.renderGraphviz(withIsolatedNodes)) }
    )
  }

  case class State(previousSuccessfulTransformation: Boolean)

  object State {
    private var current: State = State(previousSuccessfulTransformation = false)

    def update(update: State => State): State =
      State.synchronized {
        State.current = update(State.current)
        State.current
      }

    def getCurrent: State =
      State.synchronized(current)
  }

  def main(args: Array[String]): Unit = {

    val viz = new Viz()

    val topLevelDiv = document.createElement("div")
    topLevelDiv.setAttribute("class", "container-fluid")

    document.body.appendChild(topLevelDiv)

    val textIODiv = document.createElement("div")
    textIODiv.setAttribute("class", "row")
    topLevelDiv.appendChild(textIODiv)

    val optionsDiv = document.createElement("div")
    optionsDiv.setAttribute("class", "row")
    topLevelDiv.appendChild(optionsDiv)

    val actionsDiv = document.createElement("div")
    actionsDiv.setAttribute("class", "row")
    topLevelDiv.appendChild(actionsDiv)

    val emptyRowDiv = document.createElement("div")
    emptyRowDiv.setAttribute("class", "row")
    val emptyColDiv = document.createElement("div")
    emptyColDiv.setAttribute("class", "col-md-12")
    emptyRowDiv.appendChild(emptyColDiv)
    topLevelDiv.appendChild(emptyRowDiv)

    val resultsDiv = document.createElement("div")
    resultsDiv.setAttribute("class", "row")
    topLevelDiv.appendChild(resultsDiv)

    val inputTextNode = document.createElement("textarea").asInstanceOf[TextArea]
    inputTextNode.cols = 80
    inputTextNode.rows = 32
    inputTextNode.setAttribute("class", "col-md-6")
    inputTextNode.setAttribute("style", "resize:none")

    val outTextNode = document.createElement("textarea").asInstanceOf[TextArea]
    outTextNode.cols = 80
    outTextNode.rows = 32
    outTextNode.setAttribute("style", "resize:none")
    outTextNode.disabled = true

    val analyzeButton = document.createElement("button").asInstanceOf[Button]
    analyzeButton.textContent = "Analyze!"
    analyzeButton.setAttribute("class", "btn btn-default")

    val resetButton = document.createElement("button").asInstanceOf[Button]
    resetButton.textContent = "Clear"
    resetButton.setAttribute("class", "btn btn-danger")

    val enableIsolatedNodes = document.createElement("input").asInstanceOf[Input]
    enableIsolatedNodes.`type` = "checkbox"
    enableIsolatedNodes.checked = false
    enableIsolatedNodes.id = "show-isolated"

    val enableIsolatedNodesLabel = document.createElement("label").asInstanceOf[Label]
    enableIsolatedNodesLabel.textContent = "Include threads with no lock-relations"
    enableIsolatedNodesLabel.htmlFor = enableIsolatedNodes.id
    optionsDiv.appendChild(enableIsolatedNodesLabel)

    def computeAndRenderResult: Unit = {
      val reportResult = processReport(inputTextNode.value, enableIsolatedNodes.checked)
      val dotReport    = reportResult.merge

      val newState = if (reportResult.isLeft) {
        outTextNode.setAttribute("class", "col-md-6 bg-danger")
        State.update(_.copy(previousSuccessfulTransformation = false))
      } else {
        outTextNode.setAttribute("class", "col-md-6 bg-success")

        val vizOptions = Dynamic.literal(engine = "dot")

        viz.renderSVGElement(dotReport, vizOptions).toFuture.foreach { graphElement =>
          val prevResultTree = resultsDiv.childNodes
          (0 until prevResultTree.length).foreach { n =>
            val childNode = prevResultTree(n)
            resultsDiv.removeChild(childNode)
          }
          resultsDiv.appendChild(graphElement)
        }

        State.update(_.copy(previousSuccessfulTransformation = true))
      }

      outTextNode.textContent = dotReport
      analyzeButton.disabled = true
      inputTextNode.disabled = true
      enableIsolatedNodes.disabled = !newState.previousSuccessfulTransformation
    }

    analyzeButton.addEventListener("click", (e: dom.MouseEvent) => computeAndRenderResult)
    enableIsolatedNodes.addEventListener("click", (e: dom.MouseEvent) => computeAndRenderResult)

    resetButton.addEventListener(
      "click",
      (e: dom.MouseEvent) => window.location.reload(false)
    )

    textIODiv.appendChild(inputTextNode)
    textIODiv.appendChild(outTextNode)
    optionsDiv.appendChild(enableIsolatedNodes)
    actionsDiv.appendChild(analyzeButton)
    actionsDiv.appendChild(resetButton)
  }
}
