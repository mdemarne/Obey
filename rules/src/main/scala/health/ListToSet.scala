package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object ListToSet extends Rule {

  def description = "defining List.toSet is defining a Set"

  def apply = transform {
      case origin @ Term.Select(Term.Apply(Term.Name("List"), l), Term.Name("toSet")) =>
        val modified = Term.Apply(Term.Name("Set"), l)
        modified andCollect Message(s"The assignment $origin creates a useless List", origin, Some(modified))
    }.topDown

}
