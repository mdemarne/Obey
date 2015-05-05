package scala.obey

import scala.meta._

import scala.annotation.StaticAnnotation
import scala.reflect.internal.util.{ NoPosition, RangePosition, ScriptSourceFile }
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }
import scala.reflect.io.AbstractFile

import scala.meta.dialects.Scala211 // TODO: pass that as a parameter, since it might run on Dotty.

package object model {

  /** Message type returned by applying a rule */
  case class Message(message: String, originTree: Tree, modifiedTokensOpt: Option[Seq[Token]]) {
    val position = getPos(originTree)
  }

  object Message {
    def apply(message: String, originTree: Tree): Message = Message(message, originTree, None)
    def apply(message: String, originTree: Tree, modifiedTokens: Seq[Token]): Message = Message(message, originTree, Some(modifiedTokens))
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

  private def getPos(t: Tree): scala.reflect.internal.util.Position = {
    t.origin.input match {
      case in: Input.File =>
        val sourceFile = ScriptSourceFile(AbstractFile.getFile(in.f.getCanonicalPath), in.content)
        val start = t.origin.position.start.offset
        val end = t.origin.position.end.offset
        new RangePosition(sourceFile, start, start, end)
      case _ => NoPosition
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
