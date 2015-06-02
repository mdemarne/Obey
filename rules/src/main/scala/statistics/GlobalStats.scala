package health

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "Stats") object GlobalStatistics extends StatRule {

  def description = "Collecting general statistics about the current project"

  def foundTryCatch(t: Tree) = Message("trycatch-stat", t)
  def foundReturn(t: Tree) = Message("return-stat", t)
  def foundWhile(t: Tree) = Message("do-while-stat", t)
  
  def foundDef(t: Tree) = Message("def-stat", t)
  def foundVal(t: Tree) = Message("val-stat", t)
  def foundVar(t: Tree) = Message("var-stat", t)
  def foundTrait(t: Tree) = Message("trait-stat", t)
  def foundClass(t: Tree) = Message("class-stat", t)
  def foundObject(t: Tree) = Message("object-stat", t)

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
  }.topDown
}
