package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ErrorProneOneLiners extends Rule {

  def description = "Correcting error-prone one-liners"

  def apply = transform {
      case t @ Term.Select(Term.Apply(Term.Name("List"), l), Term.Name("toSet")) =>
        Term.Apply(Term.Name("Set"), l) andCollect Message(s"The assignment $t creates a useless List", t)
      // TODO: add more one-liners
    }.topDown

}
