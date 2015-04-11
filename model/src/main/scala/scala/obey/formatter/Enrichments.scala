package scala.obey.formatter

import scala.meta._

/* Contains token-specific functionality. */
class Enrichments(implicit c: semantic.Context) {

  // TODO: some funcs. might be move to scalameta in the future

  implicit class RichTokens(tokens: Vector[Token]) {
    // TODO
  }

  implicit class RichModifiedTree(tree: Tree) {

    /* Token generator based on modified tree. Since a modified tree might have parts associated with no source code,
     * it will have to token streams. This function make use of the scala.meta pretty printer and the tokenizer
     * to generate standard token output, which will be used in the various merging parts of the formatter
     */
    def generateTokens = tree.toString.tokens

  }

}
