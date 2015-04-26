package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Style") object ProhibitWhileLoop extends WarnRule {

  def description = "Prohibit calls to while and do loops - better use recursion!"

  def apply = collect {
    case t: Term.While =>
      Message(s"While loops are deprecated if you’re using a strict functional style.", t)
    case t: Term.Do =>
      Message(s"do-while loops are deprecated if you’re using a strict functional style.", t)
  }.topDown
}
