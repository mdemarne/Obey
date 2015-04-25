package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala") object SetToList extends Rule {

  def description = "defining Set.toList is defining a list but does not preserve ordering"

  def message(t: Term.Select): Message = Message(s"The assignment $t creates a list from a set and doest not preserve ordering", t)

  def apply = transform {
      case t @ Term.Select(Term.Apply(Term.Name("Set"), l), Term.Name("toList")) =>
        Term.Apply(Term.Name("List"), l) andCollect message(t)
    }.topDown

}
