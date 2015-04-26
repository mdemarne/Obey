package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Style") object EnforceCaseObject extends FixRule {

  def description = "Enforce case objects rather than empty case classes"

  def apply = transform {
    case origin @ Defn.Class(mods, n: Type.Name, ref, ctor @ Ctor.Primary(_, _, args), bdy) if args.flatten.length == 0 && mods.contains(Mod.Case()) =>
      val modified = Defn.Object(mods, Term.Name(n.value), ctor, bdy)
      modified andCollect Message("Empty case classes would better be case objects", origin, Some(modified))
  }.topDown
}
