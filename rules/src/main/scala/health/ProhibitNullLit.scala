package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ProhibitNullLit extends WarnRule {

  def description = "Prohibit null Literal"

  def apply = collect {
    case t: Lit.Null =>
      Message("The null literal should not be used in Scala. Use Options instead.", t)
  }.topDown
}
