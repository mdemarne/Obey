package health

import scala.meta.tql._

import scala.language.reflectiveCalls
import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object VarInsteadOfVal extends Rule {
  def description = "var assigned only once should be val"

  def message(n: Tree, t: Tree): Message = Message(s"The 'var' $n from ${t} was never reassigned and should therefore be a 'val'", t)

  def apply = {
    collect[Set] {
      case Term.Assign(b: Term.Name, _) => b
    }.topDown feed { assign =>
      (transform {
        case Defn.Var(a, Pat.Var.Term(b: Term.Name) :: Nil, c, Some(d)) if (!assign.contains(b)) =>
          Defn.Val(a, b :: Nil, c, d) andCollect message(b, b)
      }).topDown
    }
  }
}
