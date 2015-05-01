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

  /*def getPos(t: Tree): scala.reflect.internal.util.Position = {
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

  /* TODO: if required, move into Message */
  private def getPos(t: Tree): scala.reflect.internal.util.Position = {
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

  /* Rich manipulation on Trees - using the ScalaMeta prettyprinter if required */
  implicit class RichTree(t: Tree) {
    /* TODO: move this to show[Tokens] later, when it is implemented */
     def showTokens = t.origin match {
      case Origin.None =>      
        val newCode = t.show[Code] 
        t match {
          case _: Source => newCode.parse[Source].origin.tokens
          case _: Stat => newCode.parse[Stat].origin.tokens
        }
      case or => or.tokens
    }
  }
}
