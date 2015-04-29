package scala.obey

import scala.meta.{ Origin, Input }

import scala.annotation.StaticAnnotation
import scala.reflect.internal.util.{ NoPosition, RangePosition, ScriptSourceFile }
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }
import scala.reflect.io.AbstractFile

package object model {

  /* Message type */
  case class Message(message: String, originTree: scala.meta.Tree, modifiedTree: Option[scala.meta.Tree] = None) {
    val position = getPos(originTree)
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
        ruleTags.exists(e => posPattern.exists(p => p.matcher(e).matches)) &&
        !ruleTags.exists(e => negPattern.exists(p => p.matcher(e).matches))
      }
    }
  }

  /*def getPos(t: scala.meta.Tree): scala.reflect.internal.util.Position = {
    try {
      import scala.language.reflectiveCalls
      //val scratchpads = t.asInstanceOf[{ def internalScratchpads: Map[_, _] }].internalScratchpads
      //val associatedGtree = scratchpads.values.toList.head.asInstanceOf[List[_]].collect { case gtree: scala.reflect.internal.SymbolTable#Tree => gtree }.head
      val scratchpad = t.asInstanceOf[{ def scratchpad: Seq[Any] }].scratchpad
      val associatedGtree = scratchpad.collect { case gtree: scala.reflect.internal.SymbolTable#Tree => gtree }.head
      associatedGtree.pos
    } catch {
      case e: Exception => println(e);NoPosition
    }
  }*/

  private def getPos(t: scala.meta.Tree): scala.reflect.internal.util.Position = {
    t.origin.input match {
      case Input.None => NoPosition
      case in: Input.File =>
        val sourceFile = ScriptSourceFile(AbstractFile.getFile(in.f.getCanonicalPath), in.content)
        new RangePosition(sourceFile, t.origin.start, t.origin.start, t.origin.end)
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
