package scala.obey.formatter

import scala.meta._

/* Properly reprint a list of token in a source file. */
object Print {

  // TODO: this can be moved in an implicit inside scalameta root repository
  // TODO: is there a nicer, cleaner way which only involve wokring on tokens, regardless of the associated trees?
  def apply(tokens: Seq[scala.meta.syntactic.Token]) = tokens.map(_.code).mkString("")

}
