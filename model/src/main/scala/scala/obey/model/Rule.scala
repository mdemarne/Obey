package scala.obey.model

import scala.meta.tql._

/* Rule implementation definition */
sealed trait Rule {
  def description: String
  def apply: Matcher[List[Message]]

  override def toString = {
    val tags = this.getTags mkString ","
    val name = this.getClass.getName.split("\\$").last.split('.').last
    s"$name ($description), tags: ($tags)"
  }
}

trait FixRule extends Rule
trait StatRule extends FixRule
trait WarnRule extends Rule
