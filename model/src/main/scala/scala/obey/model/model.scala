package scala.obey

import scala.meta.{ Origin, Input }

import scala.annotation.StaticAnnotation
import scala.reflect.internal.util.{ NoPosition, RangePosition, ScriptSourceFile }
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }
import scala.reflect.io.AbstractFile

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
        /* If the positive and negative sets are empty, we take all rules and remove negatives */
        (pos.isEmpty || ruleTags.exists(e => posPattern.exists(p => p.matcher(e).matches))) &&
        (!ruleTags.exists(e => negPattern.exists(p => p.matcher(e).matches)))
      }
    }
  }

  private def getPos(t: scala.meta.Tree): scala.reflect.internal.util.Position = {
    t.origin match {
      case x: Origin.Parsed =>
          x.input match {
            case Input.None => NoPosition
            case in: Input.File =>
              val sourceFile = ScriptSourceFile(AbstractFile.getFile(in.f.getCanonicalPath), in.content)
              new RangePosition(sourceFile, x.start, x.start, x.end)
          }
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
