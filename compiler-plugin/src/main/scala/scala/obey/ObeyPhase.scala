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
        val punit = unit.body.metadata("scalameta").asInstanceOf[scala.meta.Tree]

        val messageRules = UserOption.getReport
        val formattingRules = UserOption.getFormat
        var warnings: List[Message] = Nil

        if (!messageRules.isEmpty) {
          //reporter.info(NoPosition, "Warn Rules:\n"+messageRules.mkString("\n"), true)
          warnings ++= messageRules.map(_.apply).reduce((r1, r2) => r1 +> r2)(punit)
        }

        var res: MatchResult[List[Message]] = null

        if (!formattingRules.isEmpty) {
          //reporter.info(NoPosition, "Fix Rules:\n"+formattingRules.mkString("\n"), true)
          res = formattingRules.map(_.apply).reduce((r1, r2) => r1 + r2)(punit)
          if (res.tree.isDefined && !res.result.isEmpty) {
            //Persist.archive(path)
            Persist.persist(path + ".test", res.tree.get.toString)
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