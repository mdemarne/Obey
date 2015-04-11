package scala.obey

import scala.meta.tql._

import scala.meta._
import scala.obey.model._
import scala.obey.tools.{UserOption, _}
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

        // Getting original tokens from source
        val codec = scala.io.Codec(java.nio.charset.Charset.forName("UTF-8"))
        val content = scala.io.Source.fromFile(path)(codec).mkString
        val originTokens = content.tokens

        val messageRules = UserOption.getReport
        val formattingRules = UserOption.getFormat

        var warnings: List[Message] = Nil // TODO: remove this var for a val

        /* Applying warnings */
        if (!messageRules.isEmpty) {
          warnings ++= messageRules.map(_.apply).reduce((r1, r2) => r1 +> r2)(originTree)
        }

        var res: MatchResult[List[Message]] = null // TODO: remove this var for a val

        /* Applying fixes */
        if (!formattingRules.isEmpty) {
          //reporter.info(NoPosition, "Fix Rules:\n"+formattingRules.mkString("\n"), true)
          res = formattingRules.map(_.apply).reduce((r1, r2) => r1 + r2)(originTree)
          if (res.tree.isDefined && !res.result.isEmpty) {
            //Persistence.archive(path) // TODO: uncomment
            val newTokens = formatter.Merge(originTokens, originTree, res.tree.get)
            Persistence.persist(path + ".test", formatter.Print(newTokens)) // TODO: remove .test, here for testing
            warnings ++= res.result.map(m => Message("[CORRECTED] " + m.message, m.tree))
          } else {
            warnings ++= res.result
          }
        }
        warnings.foreach(m => reporter.warning(m.position, m.message))
      }
    }
  }
}