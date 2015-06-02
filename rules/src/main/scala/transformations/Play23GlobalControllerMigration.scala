package statistics

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala-play-migration23") object Play23GlobalControllerMigration extends StatRule {

  def description = "Migrate the global controller of a play application to the newer version of Play (2.3)"

  def apply = transformDefs andThen addImport

  /* Transform all definitions to return a future */
  val transformDefs = (focus {
    case Defn.Object(_, _, _, Template(_, parents, _, _)) =>
      parents.exists(p => p match {
        case pp: Name => pp.value == "GlobalSettings"
        case _ => true
      })
  } andThen (transform {
    case origin @ Defn.Def(mods, _, _, _, tpe, body) if mods.exists(_.isInstanceOf[Mod.Override]) && body.isInstanceOf[Term.Block] =>
      val newBody = Term.Apply(Term.Select(Term.Name("Future"), Term.Name("successful")), List(body))
      origin.copy(body = newBody, decltpe = None) andCollect Message("Changing global controller return type for migration to Play 2.3", origin)
  }).topDown).topDown

  /* Add an import clause for concurrent.Future if required */
  val addImport = (transform {
    case origin: Source if !origin.stats.exists(x => x.isInstanceOf[Import] && x.tokens.contains("Future") && x.tokens.contains("concurrent")) =>
      val importFuture = Import(List(Import.Clause(Term.Select(Term.Name("scala"), Term.Name("concurrent")), List(Import.Selector.Name(Name.Indeterminate("Future"))))))
      origin.copy(stats = importFuture +: origin.stats) andCollect Message("Adding an import of Future for global controller migration to Play 2.3", origin)
  }).topDown
}
