package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Completeness") object ProhibitMagicNumber extends Rule {

  def description = "Prohibit magic numbers"

  def apply = collect {
    case x: Lit.Int =>
      Message("Better not use magic numbers!", x)
  }.topDown
}
