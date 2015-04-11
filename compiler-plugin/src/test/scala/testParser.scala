

import org.scalatest.FunSuite

import scala.obey.tools._
import scala.obey.model._

class testParser extends FunSuite {

  test("Parsing set and filtering") { // TODO: checkout why this does not pass
    UserOption.addTags("fix:+{List*}")
    assert(UserOption.getFormat.size > 0)
  }

  test("Parsing multiple arguments") {
    val (plus, minus) = SetParser.parse("+{List; Var}-{Val}")
    assert(plus.size == 2 && minus.size == 1)
  }

  test("Parsing with SetParser") {
    val res = SetParser.parse("+{List, Var} - {Dotty}")
    assert(res._1.size == 2 && res._2.size == 1)
  }
}