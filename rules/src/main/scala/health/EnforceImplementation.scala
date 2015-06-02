package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Completeness") object EnforceImplementation extends WarnRule {

  def description = "Force all implementation to be complete."

  def apply = collect {
    case t: Term.Name if t.value == "???" =>
      Message(s"Incomplete implementation", t)
  }.topDown
}
