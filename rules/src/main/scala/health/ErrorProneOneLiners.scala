package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ErrorProneOneLiners extends FixRule {

  def description = "Correcting error-prone one-liners"

  def apply = collect {
      case origin @ Term.Select(Term.Apply(Term.Name("List"), l), Term.Name("toSet")) =>
        val modified = Term.Apply(Term.Name("Set"), l)
        Message(s"The assignment creates a useless List", origin, modified.showTokens)
        case origin @ Term.Select(Term.Apply(Term.Name("Set"), l), Term.Name("toList")) =>
        val modified = Term.Apply(Term.Name("List"), l)
        Message(s"The assignment creates a useless Set. Note that the order of the element is not guaranteed.", origin, modified.showTokens)
      // TODO: add more one-liners
    }.topDown

}
