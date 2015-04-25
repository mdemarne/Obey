package scala.obey.tools

import scala.obey.model.Rule

import scala.meta.semantic.Context
import scala.util.{ Try, Failure, Success }

import java.io._
import java.net._

/* Getting all rules, depending if they are defined as objects or classes (generating new instances) */
class Loader(root: File, context: Context) {

  val classLoader = new URLClassLoader(Array(root.toURI.toURL), getClass.getClassLoader)

  private def getClasses(dir: File): List[Class[_]] = {
    if (!dir.exists) return Nil
    val dirs = dir.listFiles.filter(_.isDirectory).toList
    val classFiles = dir.listFiles.filter(f => f.isFile && f.getName.endsWith(".class")).toList

    dirs.flatMap(getClasses) ++ classFiles.map { cf =>
        val className = (dir.getCanonicalPath match {
          case dirPath if dirPath == root.getCanonicalPath => cf.getName
          case dirPath =>
            val packagePath = dirPath.substring(root.getCanonicalPath.length + 1)
            val packageName = packagePath.replace(File.separator, ".")
            s"$packageName.${cf.getName.stripSuffix(".class")}"
          }).stripSuffix(".class")
        classLoader.loadClass(className)
    }
  }

  val ruleClasses = getClasses(root).filter(c => classOf[Rule].isAssignableFrom(c))

  def rules: List[Rule] = {
    ruleClasses map { c =>
      val instance = {
        Try(c.getDeclaredField("MODULE$").get(null)) recover {
          case _: NoSuchFieldException => c.newInstance()
        } match {
          case Success(x) => x
          case Failure(_) => c.getConstructors()(0).newInstance(context)
        }
      }
      instance.asInstanceOf[Rule]
    }
  }
}
