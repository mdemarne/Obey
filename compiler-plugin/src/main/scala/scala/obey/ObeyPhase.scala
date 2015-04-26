package scala.obey

import scala.meta.tql._

import scala.meta._
import scala.obey.model._
import scala.obey.tools._
import scala.tools.nsc.Phase
import scala.tools.nsc.plugins.{PluginComponent => NscPluginComponent}
import scala.meta.dialects.Dotty
import scala.meta.ui._

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

      def apply(unit: CompilationUnit) {
        val path = unit.source.path
        val originTree = unit.body.metadata("scalameta").asInstanceOf[scala.meta.Tree]

        // TODO: this should be done by scalahost ConvertPhase
        /* Getting original tokens from source */
        val content = unit.source.content.mkString("")
        val originTokens = content.tokens

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
            if (res.tree.isDefined && !res.result.isEmpty) {
              Persistence.archive(path)
              val newTokens = formatter.Merge(originTokens, originTree, res.tree.get)
              reporter.info(NoPosition, s"Persisting changes in $path.", true)
              Persistence.persist(path, formatter.Print(newTokens))
              res.result.map(m => Message("[CORRECTED] " + m.message, m.originTree))
            } else res.result
        }

        (simpleWarnings ++ fixWarnings).foreach(m => reporter.warning(m.position, m.message))
      }
    }
  }
}
