package scala.obey.tools

import scala.obey.model.Rule
import scala.obey.model._

object UserOptions {

  /* Hold a list of rules, either to apply (positive) or to discard (negative) */
  case class TagHolder(var pos: Set[Tag], var neg: Set[Tag], var use: Boolean) {
    override def toString: String = s"+{${pos.mkString(",")}} - {${neg.mkString(",")}}"
    /* Get rules for a holder according to the 'all' filter first */
    def getRules: Set[Rule] = {
        rules.filterRules(this.pos, this.neg)
    }
    /* Get all the rules only if the compilation is allowed */
    def getAllowedRules = this.use match {
      case false => Set[Rule]()
      case true => getRules
    }
  }

  /* Saving all loaded rules */
  var rules: Set[Rule] = Set()

  /*  Various tag holders */
  private val fixes = TagHolder(Set(), Set(), false)
  private val warnings = TagHolder(Set(), Set(), true)

  /* Mapping string literals to their respective holders */
  val optMap = Map("fixes:" -> fixes, "warnings:" -> warnings)

  /* All methods to get the correct rules to apply */
  def getFixes(allowedToRunOnly: Boolean = true): Set[Rule] = if(allowedToRunOnly) fixes.getAllowedRules else fixes.getRules

  /* Avoids traversing the tree twice for format and warnings */
  def getWarnings(allowedToRunOnly: Boolean = true): Set[Rule] = (if(allowedToRunOnly) warnings.getAllowedRules else warnings.getRules) -- this.getFixes(allowedToRunOnly)

  /* Check whenever there are not rules to apply at all */
  def noRulesToApply: Boolean = fixes.getRules.isEmpty && warnings.getRules.isEmpty

  /* Update holders based on tags */
  def addTags(opts: String): Unit = optMap.find(e => opts.startsWith(e._1)) match {
    case Some((_, h)) if opts.contains("--") =>
      h.use = false
    case Some((_, h)) if opts.contains("++") =>
      h.use = true
      addTags(opts.replace("++", ""))
    case Some((s, h)) if !opts.endsWith(":") =>
      val tags = SetParser.parse(opts.substring(s.length))
      h.pos ++= tags._1
      h.neg ++= tags._2
      h.use = true
    case _ => /* Nothing to do */
  }

  override def toString = {
    optMap.mkString("\n")
  }

  def disallow: Unit = {
    fixes.use = false
    warnings.use = false
  }
}
