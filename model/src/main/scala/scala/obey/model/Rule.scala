package scala.obey.model

import scala.meta.tql._

/* Rule implementation definition */
trait Rule {

  def isFix: Boolean

  def description: String

  def apply: Matcher[List[Message]]

  override def toString = {
    val tags = this.getTags mkString ","
    val name = this.getClass.getName.split("\\$").last.split('.').last
    s"$name ($description), tags: ($tags)"
  }

}

trait FixRule extends Rule { val isFix = true }
trait WarnRule extends Rule { val isFix = true }
