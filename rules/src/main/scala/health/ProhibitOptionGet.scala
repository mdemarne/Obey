package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ProhibitOptionGet extends WarnRule {

  def description = "Fine potential Option.get calls."
  def message(get: Tree) = Message(s"This might be a get on Option - better use combinators", get)

 def apply = collect {
      // TODO: should be Term.Ref.source == get.source to ensure proper prefix information.
      // TODO: For now this rule will consider all .get methods, as this is not yet implemented
      case Term.Apply(Term.Select(_, t @ Term.Name("get")), args) if args.size == 0 => message(t)
      case s @ Term.Select(_, t @ Term.Name("get")) if !s.parent.map(_.isInstanceOf[Term.Apply]).getOrElse(false) => message(t)
    }.topDown
}
