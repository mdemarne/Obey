Obey [![Build Status](https://travis-ci.org/mdemarne/Obey.svg?branch=master)](https://travis-ci.org/mdemarne/Obey)
====



## Using Obey

Obey is  a linter tool for Scala. It enables to define health rules, which can either:

- Apply transformations to the source code;
- Print health warnings.

Obey is based on the TQL library and works on top of Scala.Meta trees, which can seamlessly be transformed from Scala trees, and can operate at both the syntactic and semantic level.

### As a compiler plugin

In order to use the Obey as a compiler plugin, that runs a phase called 'obey', use the following line:
~~~
addCompilerPlugin("com.github.mdemarne" % "sbt-compiler-plugin_2.11.6" % "0.1.0-SNAPSHOT")
~~~
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
~~~
addSbtPlugin("com.github.mdemarne" %% "sbt-obey" % "0.1­0-SNAPSHOT")
~~~

and
~~~
lazy val myProject = Project(...) enablePlugins(ObeyPlugin)
~~~

You can then set some setting keys in your project properties in order to select the rule tags you want to apply. The syntax used is the same as for the compiler plugin:

- `val obeyFixRules = settingKey[String]("List of tags and names to filter rewritting rules.")`
- `val obeyWarnRules = settingKey[String]("List of tags and names to filter warning rules.")`
- `val obeyRulesDir = settingKey[String]("Path to .class defined by the user.")`
- `val obeyRulesJar = settingKey[String]("Path to jars containing compiled rules.")

For an example of how to use Obey, refer to the [Obey-examples](https://github.com/mdemarne/Obey-examples) project.

#### Automatically add default rules

It is possible to use the rules defined by Obey in the [https://github.com/mdemarne/Obey/tree/master/rules/src/main/scala](obey-rule project) directly. To do so, simply add the project as a normal project dependency:

~~~
libraryDependencies += "com.github.mdemarne" % s"obey-rules_2.11.6"  % "0.1.0-SNAPSHOT"
~~~

Obey will automatically notice the existence of the jar and load the rules to apply. By default, only the rules with the tag `Scala` are accepted.

##### Implemented rules

Corresponding tags are put into brackets.

- health:
  - `[Scala, Style] EnforceTry`: Enforces the use of the `Try` object rather than conventional `try {...} catch {...}`
  - `[Scala, ErrorProne] SetToList`: Prevent transforming set to lists, as it does not preserve the ordering
  - `[Scala, Style] ProhibitImperativeCalls`: Prevent the call to imperative keywords such as `return`
  - `[Scala, Style] ProhibitWhileLoop`: Prevent the use of `while` and `do` - better use recursive calls
  - `[Scala, ErrorProne] ListToSet`: Avoid Creating a list and transforming it to set directly
  - `[Scala, ErrorProne] VarInsteadOfVal`: Prevent the use of vars when possible
  - `[Scala, ErrorProne] ProhibitNullLit`: Prohibit null literal
  - `[Scala, Completeness] ProhibitMagicNumber`: Prohibit magic numbers
  - `[Scala, Completeness] EnforceImplementation`: Prohibit lack of implementation (???)
  - `[Scala, Style] EnforceCaseObject`: Prohibit empty case classes: better use a case object (sometimes!)
- dotty (rules specific to Dotty):
  - `[Dotty] ExplicitImplicitTypes`: Forbidding implicit types
  - `[Dotty] EarlyInitializer`: Early initializers are not supported in Dotty
  - `[Dotty] Varargs`: Varargs are not supported in Dotty

##### Comming rules

This is a non-exhaustive list of coming rules:

- health:
  - Avoid creating threads: better use Futures
  - Enforce naming conventions
  - Prohibit the use of Option.get
  - Prohibit head and last on collections

Obey also aims to propose transformation rules (e.g. to move from one version of Play to another one), not only health rules. This is coming, too!
