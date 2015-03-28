package scala.obey

import scala.annotation.StaticAnnotation
import scala.reflect.internal.util.NoPosition
import scala.reflect.runtime.{ currentMirror => cm, universe => ru }

package object model {

  case class Holder(var pos: Set[Tag], var neg: Set[Tag], var use: Boolean) {

    override def toString: String = {
      s"+{${pos.mkString(",")}} - {${neg.mkString(",")}}"
    }
  }

  /* Message type*/
  case class Message(message: String, tree: scala.meta.Tree) {
    val position = getPos(tree)
  }

  /* Represents the tags used to handle the rule filtering*/
  case class Tag(tag: String, others: String*) extends StaticAnnotation {
    override def toString = {
      (tag :: others.toList).mkString(",")
    }
  }

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

  def getAnnotations(x: Rule): Set[String] = {
    val annot = cm.classSymbol(x.getClass).annotations.filter(a => a.tree.tpe =:= ru.typeOf[Tag]).flatMap(_.tree.children.tail)
    annot.map(y => ru.show(y).toString).map(_.replaceAll("\"", "").toLowerCase).toSet
  }
}