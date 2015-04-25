package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala") object ProhibitImperativeCall extends Rule {

  def description = "Prohibit inperative calls"

  def apply = collect {
    case x: Term.Return =>
      Message("Return is implemented as an exception and can slow your program. Use the last statement of your program as return value instead.", x)
    case x: Term.While =>
      Message(s"While loops are deprecated if you’re using a strict functional style.", x)
    case x: Term.Do =>
      Message(s"do-while loops are deprecated if you’re using a strict functional style.", x)
  }.topDown
}
