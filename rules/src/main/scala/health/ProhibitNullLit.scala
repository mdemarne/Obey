package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ProhibitNullLit extends Rule {

  def description = "Prohibit null Literal"

  def apply = collect {
    case x: Lit.Null =>
      Message("The null literal should not be used in Scala", x)
  }.topDown
}
