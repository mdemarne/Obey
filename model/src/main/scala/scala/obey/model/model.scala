package scala.obey

import scala.meta._

import scala.annotation.StaticAnnotation
import scala.reflect.internal.util.{ NoPosition, RangePosition, ScriptSourceFile }
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }
import scala.reflect.io.AbstractFile

import scala.meta.dialects.Scala211 // TODO: pass that as a parameter, since it might run on Dotty.

package object model {

  /** Message type returned by applying a rule */
  case class Message(message: String, originTree: Tree) {
    // TODO: temporary get positions passing the source file.
    // This will be removed once trees coming from scalac will be enhanced to contain
    // layout information (e.g. tokens, etc).
    // See https://github.com/scalameta/scalahost/pull/87 and https://github.com/scalameta/scalahost/pull/93 for details.
    def positionIn(path: String) = getPos(originTree, path)
  }

  /* Represents the tags used to handle the rule filtering */
  case class Tag(tag: String, others: String*) extends StaticAnnotation {
    override def toString = (tag :: others.toList) mkString ","
  }

  /* Filtering rules based on a set of positive and negative tags */
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

  /* Provigind layout information, see comment above */
  private def getPos(t: Tree, path: String): scala.reflect.internal.util.Position = {
    def withContent(content: Array[Char]): scala.reflect.internal.util.Position = {
        val sourceFile = ScriptSourceFile(AbstractFile.getFile(path), content)
        val start = t.position.start.offset
        val end = t.position.end.offset
        new RangePosition(sourceFile, start, start, end)
    }
    t.input match {
      case in: Input.File => withContent(in.chars)
      case in: Input.String => withContent(in.chars)
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
