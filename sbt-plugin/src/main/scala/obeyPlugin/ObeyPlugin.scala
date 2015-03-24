import sbt._
import Keys._
import complete.DefaultParsers._

object ObeyPlugin extends AutoPlugin {
  val obeyFixRules = settingKey[String]("List of tags to filter rewritting rules.")
  val obeyWarnRules = settingKey[String]("List of tags to filter warning rules.")
  val obeyRulesDir = settingKey[String]("Path to .class defined by the user.")

  def getJars = (fullClasspath in Runtime) map { (cp) =>
    cp.map(_.data).filter(_.name.endsWith(".jar")).mkString(":")
  }

  lazy val obeyListRules =
    Command.args("obey-list", "<args>") { (state: State, args) =>
      if(args.isEmpty){
      Project.runTask(Keys.compile in Compile,
        (Project extract state).append(Seq(scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:ListRules")), state))
      } else {
        Project.runTask(Keys.compile in Compile, 
          (Project extract state).append(Seq(scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:ListRules:"+args.mkString)), state))
      }
      state
    }

  lazy val obeyCheckCmd =
    Command.args("obey-check", "<args>") { (state: State, args) =>
      if (args.isEmpty) {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(obeyFixRules := "--", scalacOptions ++= Seq("-Ystop-after:obey")), state))
      } else {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(obeyWarnRules := args.mkString.replace(",", ";"), obeyFixRules := "--", scalacOptions ++= Seq("-Ystop-after:obey")), state))
      }
      state
    }

  lazy val obeyFixCmd =
    Command.args("obey-fix", "<args>") { (state: State, args) =>
      if (args.isEmpty) {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(obeyWarnRules := "--", scalacOptions ++= Seq("-Ystop-after:obey")), state))
      } else {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(obeyFixRules := args.mkString.replace(",", ";"), obeyWarnRules := "--", scalacOptions ++= Seq("-Ystop-after:obey")), state))
      }
      state
    }

  override lazy val projectSettings: Seq[sbt.Def.Setting[_]] = Seq(
    obeyFixRules := "",
    obeyWarnRules := "",
    obeyRulesDir := "project/rules/target/scala-2.11/classes/", // Default rule path, can be overridden.
    commands ++= Seq(obeyCheckCmd, obeyFixCmd, obeyListRules),
    addCompilerPlugin("com.github.mdemarne" % "obey-compiler-plugin_2.11.6" % "0.1.0-SNAPSHOT"),
    scalacOptions ++= Seq(
      "-P:obey:fix:" + obeyFixRules.value,
      "-P:obey:warn:" + obeyWarnRules.value,
      "-P:obey:addRules:" + obeyRulesDir.value).filterNot(x => x.endsWith(":")))
}
