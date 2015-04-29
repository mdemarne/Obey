package scala.obey.formatter

import scala.meta._
import scala.meta.dialects.Scala211

import scala.reflect.ClassTag

/* Formal a list of tokens based on the original tree and the modified tree */
object Merge {

  type Modif = (Tree, Tree)
  implicit class RichModif(mod: Modif) {
    def start = mod._1.origin.startTokenPos
    def end = mod._1.origin.endTokenPos
  }

  def apply(originTree: Tree, modifiedTree: Tree, mods: List[Modif], offset: Int = 0)(implicit c: semantic.Context): Seq[Token] = {


    debug(mods)

    // TODO: keep layout and re-apply it to modified trees
    def generateTokens(tree: Tree) = {
      val newCode = tree.show[Code]
      val newParse = tree match {
        case _: Source => newCode.parse[Source]
        case _: Stat => newCode.parse[Stat]
      }
      newParse.origin.tokens
    }

    def replaceTokens(originTokens: Seq[Token], mods: List[Modif]): Seq[Token] = mods match {
      case x :: xs =>
        /* TODO: recurse in modified trees to find similarities. This will need to keep the various offsets for token modifications in mind */
        val newTokens = generateTokens(x._2)
        val modifiedTokens = originTokens.take(x.start - offset) ++ newTokens ++ originTokens.drop(x.end + 1 - offset)
        replaceTokens(modifiedTokens, xs)
      case Nil => originTokens
    }

    /* First step, find interleaved modifications in trees */
    /*val groupedByInterleaved:List[(Modif, List[Modif])] = {
      val sortedMods = mods.groupBy(_.start).toList.map(_._2).map(_.sortBy(-_.end)).flatten
      def loop(groups: List[(Modif, List[Modif])], in: List[Modif]): List[(Modif, List[Modif])] = in match {
        case x :: xs =>
          groups.lastOption match {
            case None => 
              loop((x, Nil) :: Nil, xs)
            case Some((parent, children)) if parent.start <= x.start && parent.end >= x.end =>
              loop(groups.init.::(x, children :+ x), xs)
          }
        case Nil => groups
      }
      loop(Nil, sortedMods)
    }*/
    /* TODO: Once this is done, recurse in each interleaved modifications - this should yield a sequence of token to introduce. 
     * Note that this will essentially be a call to Merge, hence it is required that Merge can take as input a Modif as (Tree, Tree) and as (Tree, Seq[Token]) */
    
    /* second step, once the least upper bounds of modification found, sort those upper bounds */
    val sortedMods = mods.sortBy(-_._1.origin.startTokenPos)
    /* Change the token stream based on those modification from bottom to top to avoid problems in token overlaps. */
    replaceTokens(originTree.origin.tokens, sortedMods)
  }

  /*
   * TODO: check why tokens do not seems to be available from the NSC parser
   * Step1: identify parts of tree that have changed.
   *  This will need to have:
   *    1. The direct mapping between original tokens and original trees
   *        TODO: not obvious
   *    2. Mapping between original trees and modified trees
   *      => Easy, using TQL => # => But still need to find upper bounds
   * Step2: get tokens for those part of trees (using pretty printing and reparsing, as it's not available since it does not come from any source)
   * Step3: Update original toke stream based on the various modifed token stream generated above.This merge should:
   *  1. Keep layout around the modification as before
   *  2. re-add back some layout parts from the original stream of modified parts (or at least comments!)
   *  3. Not make use of string explicitly, although the only way to get token sample seems to pretty print and extract comments afterwards.
   *      TODO: there might be another, cleaner way to do this, but we would need to map trees into tokens anyways.
   *      TODO: scala-refactoring for instance uses specific "layout" classes, but add specific strings.
   */

   /* TODO: remove */
   def debug(mods: List[Modif]) = {
    println("==========================================================================")
    mods.foreach { m => 
      println(m.start + ":" + m.end) 
      println(m._1.show[Raw])
      println(m._2.show[Raw])
      println("------------------------------------------------------------------------")
    }
    println("==========================================================================")
   }
}
