package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Style") object EnforceCaseObject extends Rule {

  def description = "Enforce case objects rather than empty case classes"

  def apply = collect {
    case Defn.Class(mods, n: Type.Name, _, _, Ctor.Primary(_, _, args)) if args.flatten.length == 0 && mods.contains(Mod.Case) =>
      Message("Empty case classes would better be case objects", n)
  }.topDown
}
