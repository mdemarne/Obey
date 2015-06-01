package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Style") object ProhibitImperativeCalls extends WarnRule {

  def description = "Prohibit inperative calls"

  def apply = collect {
    case t: Term.Return =>
      Message("Return is implemented as an exception and can slow your program. Use the last statement of your program as return value instead.", t)
  }.topDown
}
