package transformations

import scala.meta._
import scala.meta.dialects.Scala211
import scala.meta.tql._

import scala.meta.internal.{ ast => impl }
import scala.obey.model._

@Tag("Scala-play-migration23", "Migration", "Demo") object Play23GlobalControllerMigration extends StatRule {

  def description = "Migrate the global controller of a play application to the newer version of Play (2.3)"

  def apply = transformDefs andThen addImport

  // Force synthetic tokens for all tree nodes using TQL
  val transformDefs = (focus {
    case impl.Defn.Object(_, _, _, impl.Template(_, parents, _, _)) =>
      parents.exists(p => p match {
        case pp: impl.Name => pp.value == "GlobalSettings"
        case _ => true
      })
  } andThen (transform {
    case x @ impl.Defn.Def(mods, _, _, _, tpe, body) if mods.exists(_.isInstanceOf[impl.Mod.Override]) && body.isInstanceOf[impl.Term.Block] =>
      val newBody = q"Future.successful($body)".asInstanceOf[impl.Term]
      x.copy(body = newBody, decltpe = None) andCollect Message("Changing global controller return type for migration to Play 2.3", x)
  }).topDown).topDown

  /* Add an import clause for concurrent.Future if required */
  val addImport = (transform {
    case s: impl.Source if !s.stats.exists(x => x.isInstanceOf[impl.Import] && x.tokens.contains("Future") && x.tokens.contains("concurrent")) =>
      val importFuture = q"import scala.concurrent.Future".asInstanceOf[impl.Stat]
      s.copy(stats = importFuture +: s.stats) andCollect Message("Adding an import of Future for global controller migration to Play 2.3", s)  
  }).topDown
}
