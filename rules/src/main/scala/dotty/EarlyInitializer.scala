package dotty

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._
import scala.meta.semantic._

@Tag("Dotty") object EarlyInitializer extends WarnRule {

  def description = "Early initializers aren't supported in Dotty"

  def apply = collect {
      case t @ Template(early, _, _, _) if early.nonEmpty =>
        Message(s"$t uses unsupported early initializer syntax", t)
    }.topDown

}
