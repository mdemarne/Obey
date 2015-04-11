package scala.obey.formatter

import scala.meta._
import scala.meta.dialects.Scala211

/**
 * Created by mdemarne on 4/11/15.
 */
object Merge {

  /* Formal a list of tokens based on the original tree and the modified tree */
  def apply(originTokens: Vector[Token], originTree: Tree, modifiedTree: Tree): Vector[Token] = {
    // TODO
    modifiedTree.toString.tokens
  }

  /*
   * TODO: check why tokens do not seems to be available from the NSC parser
   * Step1: identify parts of tree that have changed.
   * Step2: get tokens for those part of trees (using pretty printing and reparsing, as it's not available since it does not come from any source)
   * Step3: Update original toke stream based on the various modifed token stream generated above.This merge should:
   *  1. Keep layout around the modification as before
   *  2. re-add back some layout parts from the original stream of modified parts (or at least comments!)
   *  3. Not make use of string explicitly, although the only way to get token sample seems to pretty print and extract comments afterwards.
   *      TODO: there might be another, cleaner way to do this, but we would need to map trees into tokens anyways.
   *      TODO: scala-refactoring for instance uses specific "layout" classes, but add specific strings.
   */
}