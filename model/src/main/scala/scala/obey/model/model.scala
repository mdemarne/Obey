package scala.obey

import scala.annotation.StaticAnnotation
import scala.reflect.internal.util.NoPosition
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }

package object model {

  /* Message type */
  case class Message(message: String, modifiedTree: scala.meta.Tree) {
    val position = getPos(modifiedTree)
  }

  /* Represents the tags used to handle the rule filtering */
  case class Tag(tag: String, others: String*) extends StaticAnnotation {
    override def toString = (tag :: others.toList) mkString ","
  }

  implicit class RichTags(lst: Set[Rule]) {
    def filterRules(pos: Set[Tag], neg: Set[Tag]): Set[Rule] = {
      def tagsToPattern(tags: Set[Tag]) = tags map (_.tag.toLowerCase.r.pattern)
      val (posPattern, negPattern) = (tagsToPattern(pos), tagsToPattern(neg))
      lst filter { x =>
        val ruleTags = x.getTags + x.getClass.getName.split("\\$").last.toLowerCase
        pos.isEmpty || /* If the positive set is empty, we take all rules */
        ruleTags.exists(e => posPattern.exists(p => p.matcher(e).matches)) && (!ruleTags.exists(e => negPattern.exists(p => p.matcher(e).matches)))
      }
    }
  }

  // TODO: checkout if there is not a nicer way to to that using scalaMeta Origins.
  def getPos(t: scala.meta.Tree): scala.reflect.internal.util.Position = {
    try {
      import scala.language.reflectiveCalls
      //val scratchpads = t.asInstanceOf[{ def internalScratchpads: Map[_, _] }].internalScratchpads
      //val associatedGtree = scratchpads.values.toList.head.asInstanceOf[List[_]].collect { case gtree: scala.reflect.internal.SymbolTable#Tree => gtree }.head
      val scratchpad = t.asInstanceOf[{ def internalScratchpad: Seq[Any] }].internalScratchpad
      val associatedOriginal = scratchpad.collect { case x: Product if x.productPrefix == "Original" => x }.head
      val associatedGtree = associatedOriginal.asInstanceOf[{ def goriginal: Any }].goriginal.asInstanceOf[scala.reflect.internal.SymbolTable#Tree]
      associatedGtree.pos
    } catch {
      case e: Exception => NoPosition
    }
  }

  /* Using runtime mirror to get rule annotation */
  implicit class RichRule(rule: Rule) {
    def getTags: Set[String] = {
      val tags = cm.classSymbol(rule.getClass).annotations.filter(a => a.tree.tpe =:= ru.typeOf[Tag]).flatMap(_.tree.children.tail)
      tags.map(y => ru.show(y).toString).map(_.replaceAll("\"", "").toLowerCase).toSet
    }
  }
}