package health

import scala.meta.tql._

import scala.language.reflectiveCalls
//import scala.meta.internal.ast._
import scala.meta.semantic._
import scala.obey.model._
import scala.meta._
import scala.meta.dialects.Scala211

@Tag("Option", "Option.get") class OptionGet(implicit c: Context) extends Rule {

  def description = "Use combinators on options instead of explicit unwrapping"

  def message(t: Term.Ref) = Message(s"${t} should use combinators on Option", t)

  val OptionGet = t"Option".defs("get").name

  def apply = collect{
      // TODO: should be Term.Ref.source == get.source to ensure proper prefix information.
      // For now this rule will be left aside
      case get: Term.Ref if get == OptionGet =>
        sys.error("This rule does not work without the implementation of name.source.")
        message(get)
    }.topDown

}
