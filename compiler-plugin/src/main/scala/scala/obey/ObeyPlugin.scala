/**
 * 	Main component of the compiler plugin.
 *
 * 	@author Adrien Ghosn, Mathieu Demarne
 */
package scala.obey

import java.io._

import scala.obey.model._
import scala.obey.tools._

import scala.meta._
import scala.meta.internal.hosts.scalac.PluginBase
import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{ PluginComponent => NscPluginComponent }

/* TODO: move some listing logic outside of the compiler plugin if doable */
class ObeyPlugin(val global: Global) extends PluginBase with ObeyPhase {
  import global._
  implicit val context = Scalahost.mkGlobalContext(global)

  val regexp = "ListRules:\\W*-all\\W*".r.pattern
  val name = "obey"
  val description = """
      |Compiler plugin that checks defined rules against scala meta trees.
      |http://github.com/mdemarne/Obey for more information.
    """.stripMargin
  val components = List[NscPluginComponent](ConvertComponent, ObeyComponent)

  /* Loading default rules, if any */
  global.classPath.asURLs.map(_.toString).find(_.contains("obey-rules")) map { jar =>
    val path = jar.stripPrefix("file:")
    UserOptions.rules ++= new Loader(new File(path), context).loadRulesFromJar.toSet
  }

  /* Processes the options for the plugin */
  override def processOptions(options: List[String], error: String => Unit) {
    options.foreach {
      opt => opt match {
        /* Nothing to do as nothing to add */
        case _ if opt.endsWith(":") =>

        /* Rule directory */
        case _ if opt.startsWith("obeyRulesDir:") =>
          val rulesDir = opt.stripPrefix("obeyRulesDir:")
          UserOptions.rules ++= new Loader(new File(rulesDir), context).loadRulesFromDir.toSet

        /* Rule jar */
        case _ if opt.startsWith("obeyRulesjar:") =>
          val rulesDir = opt.stripPrefix("obeyRulesJar:")
          UserOptions.rules ++= new Loader(new File(rulesDir), context).loadRulesFromJar.toSet

        /* Processing options */
        case _ if UserOptions.optMap.keys.exists(s => opt.startsWith(s)) =>
          UserOptions.addTags(opt)

        /* Listing of all available rules */
        case _ if regexp.matcher(opt).matches =>
          UserOptions.disallow /* Disallow all rules, this is not applying anything */
          reporter.info(NoPosition, "List of Rules available:", true)
          reporter.info(NoPosition, UserOptions.rules.mkString("\n"), true)

        /* Listing of a set of available rules */
        case _ if opt.equals("ListRules") =>
          UserOptions.disallow /* Disallow all rules, this is not applying anything */
          val warnings = UserOptions.getWarnings(allowedToRunOnly = false)
          val fixes = UserOptions.getFixes(allowedToRunOnly = false)
          if (!warnings.isEmpty) reporter.info(NoPosition, "Warning Rules:\n" + warnings.mkString("\n"), true)
          if (!fixes.isEmpty) reporter.info(NoPosition, "Fixing Rules:\n" + fixes.mkString("\n"), true)

        case othr =>
          reporter.error(NoPosition, "Bad option for obey plugin: '" + opt + "'")
      }
    }

    /* Printing a message if no rules are to be applied */
    if (!options.exists(input => input.startsWith("listRules")) && UserOptions.noRulesToApply)
      reporter.info(NoPosition, "No Obey rules found.", true)
  }

  override val optionsHelp: Option[String] = Some("""
    | -P:obey:
    |   fix:                Specifies filters for format
    |   warn:               Specifies filter for warnings
    |   addRules:           Specifies user defined rules
    |   ListRules           Lists the rules to be used in the plugin
    |   ListRules: -all     Lists all the rules available
    |
    """.stripMargin)
}
