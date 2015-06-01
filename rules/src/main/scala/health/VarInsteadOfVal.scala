package health

import scala.meta.tql._

import scala.language.reflectiveCalls
import scala.meta.internal.ast._
import scala.obey.model._

@Tag("Scala", "ErrorProne") object VarInsteadOfVal extends FixRule {
  def description = "var assigned only once should be val"

  def apply = {
    collect[Set] {
      case Term.Assign(b: Term.Name, _) => b
    }.topDown feed { assign =>
      (transform {
        case origin @ Defn.Var(a, (p @ Pat.Var.Term(b: Term.Name)) :: Nil, c, Some(d)) if (!assign.contains(b)) =>
          val modified = Defn.Val(a, Pat.Var.Term(b) :: Nil, c, d)
          modified andCollect Message(s"The 'var' $b from ${origin} was never reassigned and should therefore be a 'val'", origin)
      }).topDown
    }
  }
}
