package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Completeness") object ProhibitMagicNumber extends WarnRule {

  def description = "Prohibit magic numbers"

  def apply = collect {
    case t: Lit.Int =>
      Message("Better not use magic numbers!", t)
  }.topDown
}
