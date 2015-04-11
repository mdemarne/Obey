package scala.obey.tools

import scala.obey.model._

import scala.language.implicitConversions
import scala.util.parsing.combinator.RegexParsers

import java.io.StringReader

object SetParser extends RegexParsers {
  //lexical.delimiters ++= List("+", "-", ",", "{","}", ";", "*")

  implicit def trad(s: String): Tag = new Tag(s)

  object Op extends Enumeration {
    type Op = Value
    val PLUS, MINUS  = Value
  }

  import Op._

  def tag: Parser[Tag] = """[\w\*]+""".r  ^^ {case e => e.replace("*", ".*") }

  def tags: Parser[Set[Tag]] = (
      "{" ~> tag ~ ("[;,]".r ~> tag).* <~ "}" ^^ {
      case e ~ Nil => Set(e)
      case e ~ e1 => Set(e) ++ e1.toSet

    })

  def set: Parser[(Set[Tag], Op)] = (
    "+" ~> tags ^^ { case e => (e, PLUS)} |
    "-" ~> tags ^^ {case e => (e, MINUS)}
    )

  def res: Parser[List[(Set[Tag], Op)]] = set.*

  def parse(str: String): (Set[Tag], Set[Tag]) = {
    parseAll(res, str) match {
      case Success(t, _) => 
        val (pos, neg) = t.partition(_._2 == PLUS)
        val posSet = pos.map(_._1).fold(Set())((x, y) => x ++ y)
        val negSet = neg.map(_._1).fold(Set())((x, y) => x ++ y)
        (posSet, negSet)
      case e => println(e)
        (Set(), Set())
    }
  }
}
