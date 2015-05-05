package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Style") object UnhealthyOneLiners extends FixRule {

  def description = "Correcting Unhealthy one lines (the things that could have a better stype)."

  def apply = collect {
	// TODO: change to allow only empty body to pass through
    case origin @ Defn.Class(mods, n: Type.Name, ref, ctor @ Ctor.Primary(_, _, args), bdy) if args.flatten.length == 0 && mods.contains(Mod.Case()) =>
      val modified = Defn.Object(mods, Term.Name(n.value), ctor, bdy)
      Message("Empty case classes would better be case objects", origin)
  }.topDown
}
