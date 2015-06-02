package statistics

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Stats") object GlobalStats extends StatRule {

  def description = "Collects general statistics about the current project."

  def foundTryCatch(t: Tree) = Message("trycatch", t)
  def foundReturn(t: Tree) = Message("return", t)
  def foundWhile(t: Tree) = Message("do-while", t)
  
  def foundDef(t: Tree) = Message("def", t)
  def foundVal(t: Tree) = Message("val", t)
  def foundVar(t: Tree) = Message("var", t)
  def foundTrait(t: Tree) = Message("trait", t)
  def foundClass(t: Tree) = Message("class", t)
  def foundObject(t: Tree) = Message("object", t)
  def foundList(t: Tree) = Message("list", t)
  def foundSet(t: Tree) = Message("set", t)
  def foundPartialFunction(t: Tree) = Message("partialFunction", t)

  def apply = collect {
    case t: Defn.Def  => foundDef(t)
    case t: Decl.Def => foundDef(t)
    case t: Defn.Val => foundVal(t)
    case t: Decl.Val => foundVal(t)
    case t: Defn.Var => foundVar(t)
    case t: Decl.Var => foundVar(t)
    case t: Defn.Trait => foundTrait(t)
    case t: Defn.Class => foundClass(t)
    case t: Defn.Object => foundObject(t)

    case t: Term.TryWithCases => foundTryCatch(t)
    case t: Term.TryWithTerm => foundTryCatch(t)
    case t: Term.Return => foundReturn(t)
    case t: Term.While => foundWhile(t)
    case t: Term.Do => foundWhile(t)
    case t: Term.Name if t.value == "List" => foundList(t)
    case t: Term.Name if t.value == "Set" =>  foundSet(t)
    case t: Term.PartialFunction => foundPartialFunction(t)

  }.topDown
}
