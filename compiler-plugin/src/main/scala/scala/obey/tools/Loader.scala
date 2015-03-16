package scala.obey.tools

import java.io._
import java.net._

import scala.obey.model.Rule
import scala.meta.semantic.Context

class Loader(val folder: String, val context: Context) {

  val root = new File(folder)
  val cl = new URLClassLoader(Array(root.toURI.toURL), getClass.getClassLoader)

  def getClasses(dir: File): List[Class[_]] = {
    if (!dir.exists) return Nil
    val dirs = dir.listFiles.filter(_.isDirectory).toList
    val classFiles = dir.listFiles.filter(f => f.isFile && f.getName.endsWith(".class")).toList
    dirs.flatMap(getClasses) ++ classFiles.map {
      cf =>
        val className = {
          if (root.getCanonicalPath == dir.getCanonicalPath) {
            cf.getName.stripSuffix(".class")
          } else {
            val packagePath = dir.getCanonicalPath.substring(root.getCanonicalPath.length + 1)
            val packageName = packagePath.replace(File.separator, ".")
            packageName + "." + cf.getName.stripSuffix(".class")
          }
        }
         cl.loadClass(className)
    }
  }

  val ruleClasses = getClasses(root).filter(c => classOf[Rule].isAssignableFrom(c))

  def rules: List[Rule] = {
    ruleClasses.map{ c =>
      val instance = try c.getDeclaredField("MODULE$").get(null)
      catch{
        case ex:NoSuchFieldException =>
          try {
            c.newInstance()
          }catch{
            case ee: Exception =>
              //println("THE CONSTRUCTORS "+c.getConstructors)
              //println("The one selected "+c.getConstructors()(0))
              c.getConstructors()(0).newInstance(context)
          }
      }
      instance.asInstanceOf[Rule]
    }
  }
}