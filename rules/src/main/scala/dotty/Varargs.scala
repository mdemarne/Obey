package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Dotty") object Varargs extends WarnRule {

  def description = "Varargs are not allowed in Dotty"
  
  def apply = collect {
    case t @ Pat.Bind(_, Pat.Arg.SeqWildcard()) =>
      Message(s"Vararg $t not supported", t)
  }.topDown
}
