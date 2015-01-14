package scala.obey.rules

import scala.meta.tql.ScalaMetaFusionTraverser._

import scala.language.reflectiveCalls
import scala.meta.internal.ast._
import scala.obey.model._
import scala.obey.tools.Enrichment._

@Tag("DCE", "Var") object UnusedMember extends Rule {
  def description = "members are defined but never used"

  def message(t: Term.Name): Message = Message(s"${t} is not used", t)

  def ignore(d: Defn): Boolean = d.isMain && d.isValueParameter && d.isConstructorArg

  def apply = {
    collect[Set] {
      case t: Defn.Def if (!ignore(t)) => t.name
    }.topDown feed { defs =>
      collect {
        case Term.Assign(b: Term.Name, _) if (defs.contains(b)) => message(b)
      }.topDown
    }
  }
}

