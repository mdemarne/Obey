package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Style") object EnforceTry extends WarnRule {

  def description = "Enforce usage of Try object, preventing usage of conventional try/catch."
  def message(t: Term) = Message(s"Instead of try/catch, use the Try object", t)

  def apply = collect {
    case t: Term.TryWithCases => message(t)
    case t: Term.TryWithTerm => message(t) 
  }.topDown
}
