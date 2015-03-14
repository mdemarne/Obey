/**
 * 	Main component of the compiler plugin.
 *
 * 	@author Adrien Ghosn
 */
package scala.obey

import scala.obey.model.Keeper
import scala.obey.model._
import scala.obey.tools._
import scala.tools.nsc.Global
import scala.tools.nsc.plugins.{ PluginComponent => NscPluginComponent }
import scala.meta.internal.hosts.scalac.PluginBase

class ObeyPlugin(val global: Global) extends PluginBase with ObeyPhase {
  import global._
  implicit val context = scala.meta.internal.hosts.scalac.Scalahost.mkSemanticContext(global)

  val regexp = "ListRules:\\W*-all\\W*".r.pattern
  val name = "obey"
  val description = """Compiler plugin that checks defined rules against scala meta trees.
  http://github.com/mdemarne/Obey for more information."""
  val components = List[NscPluginComponent](ConvertComponent, ObeyComponent)

  /* Processes the options for the plugin*/
  override def processOptions(options: List[String], error: String => Unit) {
    options.foreach {
      opt =>
        if (opt.endsWith(":")) {
          //Nothing to do
        } else if (opt.startsWith("addRules:")) {
          val opts = opt.substring("addRules:".length)
          Keeper.loadedRules = new Loader(opts, context).rules.toSet
          // reporter.info(NoPosition, "Obey add rules from: " + opts, true)
        } else if (UserOption.optMap.keys.exists(s => opt.startsWith(s))) {
          UserOption.addTags(opt)
          // reporter.info(NoPosition, "Tag Filters:\n" + UserOption.toString, true)
        } else if (regexp.matcher(opt).matches) {
          UserOption.disallow
          // reporter.info(NoPosition, "List of Rules available:", true)
          // reporter.info(NoPosition, Keeper.rules.mkString("\n"), true)
        } else if (opt.equals("ListRules")) {
          // reporter.info(NoPosition, "List of selected Rules:", true)
          val reports = UserOption.getReport
          val fixes = UserOption.getFormat
          if (!reports.isEmpty)
            reporter.info(NoPosition, "Warn Rules:\n" + reports.mkString("\n"), true)
          if (!fixes.isEmpty)
            reporter.info(NoPosition, "Fix Rules:\n" + fixes.mkString("\n"), true)
          UserOption.disallow
        } else {
          reporter.error(NoPosition, "Bad option for obey plugin: '" + opt + "'")
        }
    }
  }

  override val optionsHelp: Option[String] = Some("""
    | -P:obey:
    |   all:                Specifies filters for all
    |   fix:                Specifies filters for format
    |   warn:               Specifies filter for warnings
    |   addRules:           Specifies user defined rules
    |   ListRules           Lists the rules to be used in the plugin
    |   ListRules: -all     Lists all the rules available
    |
    """)
}