package scala.obey.formatter

import scala.meta._

/* Properly reprint a list of token in a source file. */
object Print {

  // TODO: this can be moved in an implicit inside scalameta root repository
  def apply(tokens: Vector[Token]) = tokens.map(_.code).mkString("")

}
