

import org.scalatest.FunSuite

import scala.obey.tools._
import scala.obey.model._

import scala.meta.internal.ast._
import scala.meta.tql._

class TagTest extends FunSuite {

  @Tag("Dummy") case object DummyRule extends FixRule {
    val description ="Dummy Rule for testing"
    val apply = collect {
      case x: Defn.Def => Message("Found a Def!", x)
    }
  }

  test("Parsing set and filtering") {
    UserOptions.rules += DummyRule
    UserOptions.addTags("fixes:+{Dummy*}")
    assert(UserOptions.rules.size > 0)
    assert(UserOptions.getFixes().size > 0)
  }

  test("Removing rule to filer") {
    UserOptions.addTags("fixes:-{Dummy*}")
    assert(UserOptions.rules.size > 0)
    assert(UserOptions.getFixes().size == 0)
  }

  test("Parsing multiple arguments") {
    val (plus, minus) = SetParser.parse("+{List; Var} - {Val}")
    assert(plus.size == 2 && minus.size == 1)
  }

  test("Parsing with SetParser") {
    val res = SetParser.parse("+{List, Var} - {Dotty}")
    assert(res._1.size == 2 && res._2.size == 1)
  }
}
