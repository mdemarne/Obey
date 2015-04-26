package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

/* TODO: cover assignment cases such as val (xx, yy) = (x.toList, y.toList) */
@Tag("Scala", "ErrorProne") object SetToList extends Rule {

  def description = "defining Set.toList is defining a list but does not preserve ordering"

  def message(t: Term.Select): Message = Message(s"The assignment $t creates a list from a set and doest not preserve ordering", t)

  def apply = (collect[Set] {
      case sel @ Term.Select(x: Term.Name, Term.Name("toList")) => (x, sel)
    }.topDown feed { sets =>
      collect {
        case t @ Defn.Val(_, Pat.Var.Term(n: Term.Name) :: Nil, _, Term.Apply(Term.Name("Set"), _)) if sets.exists(_._1 == n) =>
          message(sets.find(_._1 == n).get._2)
        case t @ Defn.Var(_, Pat.Var.Term(n: Term.Name) :: Nil, _, Some(Term.Apply(Term.Name("Set"), _))) if sets.exists(_._1 == n) =>
          message(sets.find(_._1 == n).get._2)
        case t @ Term.Select(Term.Apply(Term.Name("Set"), l), Term.Name("toList")) =>
          message(t)
      }.topDown
    })
}
