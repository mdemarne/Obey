Obey [![Build Status](https://travis-ci.org/mdemarne/Obey.svg?branch=master)](https://travis-ci.org/mdemarne/Obey)
====



## Using Obey

Obey is  a linter tool for Scala. It enables to define health rules, which can either:

- Apply transformations to the source code;
- Print health warnings.

Obey is based on the TQL library and works on top of Scala.Meta trees, which can seamlessly be transformed from Scala trees, and can operate at both the syntactic and semantic level.

### As a compiler plugin

In order to use the Obey as a compiler plugin, that runs a phase called 'obey', use the following line:
```scala
addCompilerPlugin("com.github.mdemarne" % "sbt-compiler-plugin_2.11.6" % "0.1.0-SNAPSHOT")
```
The compiler plugin defines the following options:
* `warnings:<OFL>` enables to select the rules that will only generate warnings
* `fixes:<OFL>` enables to select the rules that will automatically correct the source code
* `obeyRulesDir:<path to compiled classes>` enables to add user-defined rules to a project

The option filtering language (OFL) correponds to the following parser:
~~~
tag := “[\w\*]+”.r
tags := “{” ~ tag ~ (“[;,]”.r ~ tag).* ~ “}”
OFL := ("[+-]".r ~ tags).*
~~~

For example the following expression
~~~
+{List*, Set} - {Array}
~~~
selects all the rules that have a tag/name begining by List or equal to Set, while excluding all the ones that have a tag/name equal to Array. By default, all Scala rules are selected as warnings

### As a SBT autoplugin

In order to use Obey as an SBT autoplugin, use the following line:
```scala
addSbtPlugin("com.github.mdemarne" %% "sbt-obey" % "0.1­0-SNAPSHOT")
 ```
and
```scala
lazy val myProject = Project(...) enablePlugins(ObeyPlugin)
```

You can then set some setting keys in your project properties in order to select the rule tags you want to apply. The syntax used is the same as for the compiler plugin:

- `val obeyRules = settingKey[String]("List of tags to filter all rules.")`
- `val obeyRulesDir = settingKey[String]("Path to .class defined by the user.")`
- `val obeyRulesJar = settingKey[String]("Path to jars containing compiled rules.")

And if you would like to be more precise, you can specify the tags for the fixing rules and the checking rules separately. This overrides the more precise setup of obeyFixRules and obeyWarnRules:

- `val obeyFixRules = settingKey[String]("List of tags and names to filter rewritting rules.")`
- `val obeyWarnRules = settingKey[String]("List of tags and names to filter warning rules.")`

For example:
```scala
lazy val root = (project in file(".")).
  settings (
    ObeyPlugin.obeyRules := "+{Scala*} - {Completeness*} - {Dotty*} - {Mine*}",
    ObeyPlugin.obeyRulesJar := "~/home/me/my/jar/rules.jar"
  ) enablePlugins(ObeyPlugin)
```

You can the run obey in different flavours:

- `obey-check`: will print warnings from all activated rules
- `obey-fix`: will fix the source code based on the fixing rules
- `obey-fix-dryrun`: will print warnings for the fixing rules, without modifying the source code.

By default, Obey applies all warning rules for the tag `Scala` and none for the fixing rules. For a real example of how to use Obey, refer to the [Obey-examples](https://github.com/mdemarne/Obey-examples) project.

## Automatically add default rules

It is possible to use the rules defined by Obey in the [https://github.com/mdemarne/Obey/tree/master/rules/src/main/scala](obey-rule project) directly. To do so, simply add the project as a normal project dependency:

```scala
libraryDependencies += "com.github.mdemarne" % s"obey-rules_2.11.6"  % "0.1.0-SNAPSHOT"
```

Obey will automatically notice the existence of the jar and load the rules to apply. By default, only the rules with the tag `Scala` are accepted.

### Implemented rules

Corresponding tags are put into brackets.

- health:
  - `[Scala, Style] EnforceTry`: Enforces the use of the `Try` object rather than conventional `try {...} catch {...}`
  - `[Scala, ErrorProne] SetToList`: Prevent transforming set to lists, as it does not preserve the ordering
  - `[Scala, Style] ProhibitImperativeCalls`: Prevent the call to imperative keywords such as `return`
  - `[Scala, Style] ProhibitWhileLoop`: Prevent the use of `while` and `do` - better use recursive calls
  - `[Scala, ErrorProne] ErrorProneOneLiners`: Correcting error-prone one-liners
  - `[Scala, ErrorProne] VarInsteadOfVal`: Prevent the use of vars when possible
  - `[Scala, ErrorProne] ProhibitNullLit`: Prohibit null literal
  - `[Scala, ErrorProne] ProhibitOptionGet`: Prohibit call to get on Options.
  - `[Scala, Completeness] ProhibitMagicNumber`: Prohibit magic numbers
  - `[Scala, Completeness] EnforceImplementation`: Prohibit lack of implementation (???)
  - `[Scala, Style] UnhealyOneLiner`: Return warning for some one nice liners (for example, having a case class with no body and no parameter is not really interesting, better use a case object)!
- Statistics:
  - `[Scala, Stats] GlobalStasts`:Collects general statistics about the current project
- dotty (rules specific to Dotty):
  - `[Dotty] ExplicitImplicitTypes`: Forbidding implicit types
  - `[Dotty] EarlyInitializer`: Early initializers are not supported in Dotty
  - `[Dotty] Varargs`: Varargs are not supported in Dotty
- Transformations (early prototype):
  - `[Scala-play-migration23] Play23GlobalControllerMigration`: Migrate the global controller of a play application to the newer version of Play (2.3)

### Coming rules

This is a non-exhaustive list of coming rules:

- health:
  - Avoid creating threads: better use Futures
  - Enforce naming conventions
  - Prohibit head and last on collections

Obey also aims to propose transformation rules (e.g. to move from one version of Play to another one), not only health rules.

## Adding rules

You can add rules either to your own project build (as shown in the [Obey-examples](https://github.com/mdemarne/Obey-examples) project.) or propose pull request to this project.

A rule either extends the `WarnRule` trait, the `StatRule` trait or the `FixRule` trait, depending of its nature. `WarnRule` simply output a message, but no modification done to the trees using TQL are persisted. `StatRule` behave the same way, but the messages are counted by message string, and output at the end of the compilation process as a table showing statistics. `FixRule` on the other hand will see any modifications persisted in the code when run with `obey-fix`.

A rule has the following structure:

```scala
import scala.meta.tql._

import scala.language.reflectiveCalls
import scala.meta.internal.ast._
import scala.obey.model._

@Tag("<your>", "<tag>") object YourRule extends FixRule {
  def description = "<The description of the rule>"

  def apply = ???
}
```

The apply method uses the TQL library. For more details on how to use TQL, see the [TQL technical paper](http://infoscience.epfl.ch/record/204789/files/TraversableQueryLanguage.pdf). The apply methods should return a `Matcher` containing warning messages. Those warning messages are defined in [mode.scala](https://github.com/mdemarne/Obey/blob/master/model/src/main/scala/scala/obey/model/model.scala#L13) and have the following signature:

```scala
case class Message(
  message: String,
  originTree: scala.meta.Tree
)
```

The `message` is the string that will be printed, the `originTree` is the subtree that raised the warning (and its position will be used by the compilation reporter).
