package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala") object ProhibitImperativeCalls extends Rule {

  def description = "Prohibit inperative calls"

  def apply = collect {
    case x: Term.Return =>
      Message("Return is implemented as an exception and can slow your program. Use the last statement of your program as return value instead.", x)
  }.topDown
}
