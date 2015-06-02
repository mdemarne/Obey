package scala.obey

import scala.meta.tql._
import scala.meta._
import scala.obey.model._
import scala.obey.tools._
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins.{PluginComponent => NscPluginComponent}

/* Definition of the Obey phase added to NSC */
trait ObeyPhase {
  self: ObeyPlugin =>

  object ObeyComponent extends NscPluginComponent {
    val global: self.global.type = self.global
    implicit val context = Scalahost.mkGlobalContext(global)

    import global._

    val phaseName = "obey"
    override val runsAfter = List("typer")
    override val runsRightAfter = Some("convert")

    override def description = "apply obey rules"

    def newPhase(prev: Phase): Phase = new StdPhase(prev) {

      var stats: List[Message] = Nil

      def apply(unit: CompilationUnit) {
        val path = unit.source.path
        val originTree = unit.body.metadata("scalametaSyntactic").asInstanceOf[scala.meta.Tree]

        /* Applying warnings */
        val simpleWarnings: List[Message] = UserOptions.getWarnings() match {
          case lst if lst.isEmpty => Nil
          case lst => lst.map(_.apply).reduce((r1, r2) => r1 +> r2)(originTree)
        }

        /* Applying fixes, saving changes and returning warnings */
        val fixWarnings: List[Message] = UserOptions.getFixes() match {
          case lst if lst.isEmpty => Nil
          case lst =>
            val res = lst.map(_.apply).reduce((r1, r2) => r1 + r2)(originTree)
            if (res.tree.isDefined && !res.result.isEmpty && !UserOptions.dryrun) {
              // Persistence.archive(path) // TODO: uncomment
              reporter.info(NoPosition, s"Persisting changes in $path.", true)
              Persistence.persist(path + ".test", res.tree.get.tokens.map(_.show[Code]).mkString) // TODO: remove .test
              res.result.map (m => Message("[FIXED] " + m.message, m.originTree))
            } else res.result
        }

        /* Generating statistics */
        val localStats: List[Message] = UserOptions.getStats() match {
          case lst if lst.isEmpty => Nil
          case lst => lst.map(_.apply).reduce((r1, r2) => r1 +> r2)(originTree)
        }
        stats ++= localStats /* Adding to the global count */

        if (!stats.isEmpty) {
          reporter.info(NoPosition, "Global statistics:", true)
          val groupedStats = stats.groupBy(m => m.message)
          val max = groupedStats.map(_._1.length).max
          val firstLineSpaces = " " * (max - 4)
          reporter.info(NoPosition, s"type$firstLineSpaces\tcount", true)
          groupedStats.foreach { m =>
            val spaces = " " * (max - m._1.length)
            reporter.info(NoPosition, s"${m._1}$spaces\t${m._2.size}", true)
          }
        }

        (simpleWarnings ++ fixWarnings).foreach(m => reporter.warning(m.positionIn(path), m.message))
      }
    }
  }
}
