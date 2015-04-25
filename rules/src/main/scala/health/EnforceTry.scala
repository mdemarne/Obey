package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala") object EnforceTry extends Rule {

  def description = "Enforce usage of Try object, preventing usage of conventional try/catch."

  def apply = collect {
    case t: Term.TryWithCases =>
      Message(s"Instead of try/catch, you should use the Try object", t)
  }.topDown
}