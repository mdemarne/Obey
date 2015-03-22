import sbt.Keys._
import sbt._

object build extends Build {
  import Dependencies._
  import PublishSettings._
  import Settings._

  lazy val commonDependencies = Seq(
    libraryDependencies <++= (scalaVersion)(sv => Seq(
      Dependencies.meta,
      Dependencies.scalatest))
  )

  lazy val pluginCompiler = Project(
    id = "compiler-plugin",
    base = file("compiler-plugin"),
    settings = sharedSettings ++ publishableSettings ++ commonDependencies ++ mergeDependencies ++ List(
      libraryDependencies <++= (scalaVersion)(sv => Seq(
        compiler(sv) % "provided",
        Dependencies.scalahost)),
      name := "obey-compiler-plugin",
      resourceDirectory in Compile := baseDirectory.value / "resources")) dependsOn (model)

  lazy val model = Project(
    id = "model",
    base = file("model"),
    settings = sharedSettings ++ publishableSettings ++ commonDependencies ++ List(
      libraryDependencies <++= (scalaVersion)(sv => Seq(
        reflect(sv))),
      name := "obey-model"
    ))

  lazy val rules = Project(
    id = "rules",
    base = file("rules"),
    settings = sharedSettings ++ publishableSettings ++ commonDependencies
  ) dependsOn(model)

  lazy val pluginSbt = Project(
    id = "sbt-plugin",
    base = file("sbt-plugin"),
    settings = sbtPluginSettings ++ publishableSettings ++ List(sbtPlugin := true, name := "sbt-obey"))
}
