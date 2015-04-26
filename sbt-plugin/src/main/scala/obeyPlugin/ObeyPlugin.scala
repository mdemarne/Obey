import sbt._
import Keys._
import complete.DefaultParsers._

object ObeyPlugin extends AutoPlugin {
  val obeyFixRules = settingKey[String]("List of tags to filter rewritting rules.")
  val obeyWarnRules = settingKey[String]("List of tags to filter warning rules.")
  val obeyRulesDir = settingKey[String]("Path to .class defined by the user.")
  val obeyRulesJar = settingKey[String]("Path to jars containing compiled rules.")

  lazy val obeyListRules =
    Command.args("obey-list", "<args>") { (state: State, args) =>
      if(args.isEmpty) {
      Project.runTask(Keys.compile in Compile,
        (Project extract state).append(Seq(
          scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:ListRules"),
          excludeFilter in unmanagedSources := "*.java"
        ), state))
      } else {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(
            scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:ListRules:"+args.mkString),
            excludeFilter in unmanagedSources := "*.java"
          ), state))
      }
      state
    }

  lazy val obeyCheckCmd =
    Command.args("obey-check", "<args>") { (state: State, args) =>
      if (args.isEmpty) {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(
            scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:dryrun"),
            excludeFilter in unmanagedSources := "*.java"
          ), state))
      } else {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(
            obeyWarnRules := args.mkString.replace(",", ";"),
            scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:dryrun"),
            excludeFilter in unmanagedSources := "*.java"
          ), state))
      }
      state
    }

  lazy val obeyFixCmd =
    Command.args("obey-fix", "<args>") { (state: State, args) =>
      if (args.isEmpty) {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(
            obeyWarnRules := "--",
            scalacOptions ++= Seq("-Ystop-after:obey"),
            excludeFilter in unmanagedSources := "*.java"
          ), state))
      } else {
        Project.runTask(Keys.compile in Compile,
          (Project extract state).append(Seq(
            obeyFixRules := args.mkString.replace(",", ";"),
            obeyWarnRules := "--",
            scalacOptions ++= Seq("-Ystop-after:obey"),
            excludeFilter in unmanagedSources := "*.java"
          ), state))
      }
      state
    }

    lazy val obeyFixDryRunCmd =
      Command.args("obey-fix-dryrun", "<args>") { (state: State, args) =>
        if (args.isEmpty) {
          Project.runTask(Keys.compile in Compile,
            (Project extract state).append(Seq(
              obeyWarnRules := "--",
              scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:dryrun"),
              excludeFilter in unmanagedSources := "*.java"
            ), state))
        } else {
          Project.runTask(Keys.compile in Compile,
            (Project extract state).append(Seq(
              obeyFixRules := args.mkString.replace(",", ";"),
              obeyWarnRules := "--",
              scalacOptions ++= Seq("-Ystop-after:obey", "-P:obey:dryrun"),
              excludeFilter in unmanagedSources := "*.java"
            ), state))
        }
        state
      }

  override lazy val projectSettings: Seq[sbt.Def.Setting[_]] = Seq(
    obeyFixRules := "",
    obeyWarnRules := "+{Scala}-{Dotty}",
    obeyRulesDir := "project/rules/target/scala-2.11/classes/", // Default rule path, can be overridden.
    obeyRulesJar := "", // No default jar
    commands ++= Seq(obeyCheckCmd, obeyFixCmd, obeyFixDryRunCmd, obeyListRules),
    addCompilerPlugin("com.github.mdemarne" % "obey-compiler-plugin_2.11.6" % "0.1.0-SNAPSHOT"),
    scalacOptions ++= Seq(
      "-P:obey:fixes:" + obeyFixRules.value,
      "-P:obey:warnings:" + obeyWarnRules.value,
      "-P:obey:obeyRulesDir:" + obeyRulesDir.value,
      "-P:obey:obeyRulesJar:" + obeyRulesJar.value).filterNot(x => x.endsWith(":")))
}
