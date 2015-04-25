package scala.obey.tools

import scala.obey.model.Rule

import scala.meta.semantic.Context
import scala.util.{ Try, Failure, Success }

import java.io._
import java.net._
import java.util.zip.{ZipFile, ZipEntry}

import scala.collection.JavaConversions._

/* Getting all rules, depending if they are defined as objects or classes (generating new instances) */
class Loader(root: File, context: Context) {

  def loadRulesFromDir: List[Rule] = {
    val classLoader = new URLClassLoader(Array(root.toURI.toURL), getClass.getClassLoader)
    def load(dir: File): List[Class[_]] = {
      if (!dir.exists) return Nil
      val dirs = dir.listFiles.filter(_.isDirectory).toList
      val classFiles = dir.listFiles.filter(f => f.isFile && f.getName.endsWith(".class")).toList

      dirs.flatMap(load) ++ classFiles.map { cf =>
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
    generateRuleInstances(load(root))
  }

  def loadRulesFromJar: List[Rule] = {
    val classLoader = new URLClassLoader(Array(new URL(s"jar:file:${root.getPath}!/")), getClass.getClassLoader)
    val zip = new ZipFile(root)
    val entries = zip.entries.toSet.toList
    val names = entries.filter(e => !e.isDirectory && e.getName.endsWith(".class")).map(_.getName.replaceAll("/", ".").stripSuffix(".class"))
    val classes = names.map(n => classLoader.loadClass(n))
    generateRuleInstances(classes)
  }

  private def generateRuleInstances(classes: List[Class[_]]): List[Rule] = {
    val ruleClasses = classes.filter(c => classOf[Rule].isAssignableFrom(c))
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
