package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ErrorProneOneLiners extends FixRule {

  def description = "Correcting error-prone one-liners"

  def apply = transform {
      case origin @ Term.Select(Term.Apply(Term.Name("List"), l), Term.Name("toSet")) =>
        val modified = Term.Apply(Term.Name("Set"), l)
        modified andCollect Message(s"The assignment creates a useless List", origin)
        case origin @ Term.Select(Term.Apply(Term.Name("Set"), l), Term.Name("toList")) =>
        val modified = Term.Apply(Term.Name("List"), l)
        modified andCollect Message(s"The assignment creates a useless Set. Note that the order of the element is not guaranteed.", origin)
      // TODO: add more one-liners
    }.topDown

}
