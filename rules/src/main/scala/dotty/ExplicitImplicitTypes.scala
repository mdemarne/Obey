package dotty

import scala.meta.tql._

import scala.meta.internal.ast._
import scala.obey.model._
import scala.meta.semantic._

import scala.language.implicitConversions

@Tag("Dotty") class ExplicitImplicitTypes(implicit c: Context) extends FixRule {

  def description = "Type inference for return types of implicit vals and defs isn't supported in Dotty"

  def message(origin: Tree, tpe: Type) = Message(s"result type ($tpe) of implicit definition needs to be given explicitly", origin)

  /* Casting from one ast.Type to the internal one */
  implicit def tpeCast(tpe: scala.meta.Type) = tpe.asInstanceOf[scala.meta.internal.ast.Type]

  def apply = transform {
    case origin @ Defn.Val(mods, (name: Term.Name) :: Nil, None, rhs) if mods.exists(_.isInstanceOf[Mod.Implicit]) =>
      val modified = Defn.Val(mods, (name: Term.Name) :: Nil, Some(rhs.tpe), rhs) 
      modified andCollect message(origin, rhs.tpe)
    case origin @ Defn.Def(mods, name, tparams, paramss, None, body) if origin.isImplicit =>
      val modified = Defn.Def(mods, name, tparams, paramss, Some(body.tpe), body) 
      modified andCollect message(origin, body.tpe)
  }.topDown
}
