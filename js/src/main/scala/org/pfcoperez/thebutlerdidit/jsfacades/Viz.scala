package org.pfcoperez.thebutlerdidit.jsfacades

import scalajs.js
import scalajs.js.annotation._
import scalajs.js.Promise
import org.scalajs.dom
import org.scalajs.dom.Element

@js.native
@JSGlobal
class Viz extends js.Object {
  def renderSVGElement(src: String, options: js.Object = js.Object()): Promise[Element] = js.native
}
