import sbt._
import Keys._
import complete.DefaultParsers._

object ObeyPlugin extends AutoPlugin {
  val obeyFixRules = settingKey[String]("List of tags to filter rewritting rules.")
  val obeyWarnRules = settingKey[String]("List of tags to filter warning rules.")
  val obeyRules = settingKey[String]("List of tags to filter all rules. This overrides the more precise setup of obeyFixRules and obeyWarnRules.")
  val obeyRulesDir = settingKey[String]("Path to .class defined by the user.")
  val obeyRulesJar = settingKey[String]("Path to jars containing compiled rules.")

  def createObeyCommand(command: String)(settings: Seq[String] => Seq[Setting[_]]) = {
    val baseSettings = Seq(
      scalacOptions ++= Seq("-Ystop-after:obey"),
      excludeFilter in unmanagedSources := "*.java")
    Command.args(command, "<args>") { (state: State, args) =>
      Project.runTask(Keys.compile in Compile,
        (Project extract state)
          .append(baseSettings ++ settings(args),
            state))
      state
    }
  }

  lazy val obeyListRules = createObeyCommand("obey-list")(
    (args: Seq[String]) => args match {
      case Seq() => Seq(scalacOptions += "-P:obey:ListRules")
      case _ => Seq(scalacOptions += "-P:obey:ListRules:" + args.mkString)
    })

  lazy val obeyCheckCmd = createObeyCommand("obey-check")(
    (args: Seq[String]) => args match {
      case Seq() => Seq(scalacOptions += "-P:obey:dryrun")
      case _ => Seq(scalacOptions += "-P:obey:dryrun", obeyWarnRules := args.mkString.replace(",", ";"))
    })

  lazy val obeyFixCmd = createObeyCommand("obey-fix")(
    (args: Seq[String]) => args match {
      case Seq() => Seq(obeyWarnRules := "--")
      case _ => Seq(obeyWarnRules := "--", obeyFixRules := args.mkString.replace(",", ";"))
    })

  lazy val obeyFixDryRunCmd = createObeyCommand("obey-fix-dryrun")(
    (args: Seq[String]) => args match {
      case Seq() => Seq(obeyWarnRules := "--", scalacOptions += "-P:obey:dryrun")
      case _ => Seq(obeyWarnRules := "--", obeyFixRules := args.mkString.replace(",", ";"), scalacOptions += "-P:obey:dryrun")
    })

  override lazy val projectSettings: Seq[sbt.Def.Setting[_]] = Seq(
    obeyFixRules := "",
    obeyWarnRules := "+{Scala}-{Dotty}",
    obeyRules := "",
    obeyRulesDir := "project/rules/target/scala-2.11/classes/", // Default rule path, can be overridden.
    obeyRulesJar := "", // No default jar
    commands ++= Seq(obeyCheckCmd, obeyFixCmd, obeyFixDryRunCmd, obeyListRules),
    addCompilerPlugin("com.github.mdemarne" % "obey-compiler-plugin_2.11.6" % "0.1.0-SNAPSHOT"),
    scalacOptions ++= Seq(
      "-P:obey:fixes:" + (obeyRules.value match {
        case "" => obeyFixRules.value
        case r => r
      }),
      "-P:obey:warnings:" + (obeyRules.value match {
        case "" => obeyWarnRules.value
        case r => r
      }),
      "-P:obey:obeyRulesDir:" + obeyRulesDir.value,
      "-P:obey:obeyRulesJar:" + obeyRulesJar.value).filterNot(x => x.endsWith(":")))
}
