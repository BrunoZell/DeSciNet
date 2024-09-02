import Dependencies.*
import sbt.*

ThisBuild / organization := "com.descinet"
ThisBuild / scalaVersion := "2.13.10"
ThisBuild / evictionErrorLevel := Level.Warn

ThisBuild / assemblyMergeStrategy := {
  case "logback.xml" => MergeStrategy.first
  case x if x.contains("io.netty.versions.properties") => MergeStrategy.discard
  case PathList(xs@_*) if xs.last == "module-info.class" => MergeStrategy.first
  case x =>
    val oldStrategy = (assembly / assemblyMergeStrategy).value
    oldStrategy(x)
}

lazy val root = (project in file(".")).
  settings(
    name := "descinet"
  ).aggregate(sharedData, currencyL0, currencyL1, dataL1)

lazy val sharedData = (project in file("modules/shared_data"))
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "descinet-shared_data",
    scalacOptions ++= List(
      "-Ymacro-annotations",
      "-Yrangepos",
      "-Wconf:cat=unused:silent", // Silence all unused warnings
      "-Wconf:cat=unused-nowarn:silent", // Silence unused warnings with @nowarn
      "-Wconf:msg=parameter value update in method validateModelId is never used:silent", // Silence specific unused parameter warning
      "-Wconf:cat=unused-imports:silent", // Silence unused import warnings
      "-Wconf:cat=unused-params:silent", // Silence unused parameter warnings
      "-language:reflectiveCalls"
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.descinet.shared_data",
    resolvers += Resolver.mavenLocal,
    resolvers += Resolver.githubPackages("abankowski", "http-request-signer"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.semanticDB,
      Libraries.tessellationNodeShared
    )
  )

lazy val currencyL1 = (project in file("modules/l1"))
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .settings(
    name := "descinet-currency-l1",
    scalacOptions ++= List(
      "-Ymacro-annotations",
      "-Yrangepos",
      "-Wconf:cat=unused:silent", // Silence all unused warnings
      "-Wconf:cat=unused-nowarn:silent", // Silence unused warnings with @nowarn
      "-Wconf:msg=parameter value update in method validateModelId is never used:silent", // Silence specific unused parameter warning
      "-Wconf:cat=unused-imports:silent", // Silence unused import warnings
      "-Wconf:cat=unused-params:silent", // Silence unused parameter warnings
      "-language:reflectiveCalls"
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.descinet.l1",
    resolvers += Resolver.mavenLocal,
    resolvers += Resolver.githubPackages("abankowski", "http-request-signer"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.semanticDB,
      Libraries.tessellationCurrencyL1
    )
  )

lazy val currencyL0 = (project in file("modules/l0"))
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .dependsOn(sharedData)
  .settings(
    name := "descinet-currency-l0",
    scalacOptions ++= List(
      "-Ymacro-annotations",
      "-Yrangepos",
      "-Wconf:cat=unused:silent", // Silence all unused warnings
      "-Wconf:cat=unused-nowarn:silent", // Silence unused warnings with @nowarn
      "-Wconf:msg=parameter value update in method validateModelId is never used:silent", // Silence specific unused parameter warning
      "-Wconf:cat=unused-imports:silent", // Silence unused import warnings
      "-Wconf:cat=unused-params:silent", // Silence unused parameter warnings
      "-language:reflectiveCalls"
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.descinet.l0",
    resolvers += Resolver.mavenLocal,
    resolvers += Resolver.githubPackages("abankowski", "http-request-signer"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.semanticDB,
      Libraries.declineRefined,
      Libraries.declineCore,
      Libraries.declineEffect,
      Libraries.tessellationCurrencyL0
    )
  )

lazy val dataL1 = (project in file("modules/data_l1"))
  .enablePlugins(AshScriptPlugin)
  .enablePlugins(BuildInfoPlugin)
  .enablePlugins(JavaAppPackaging)
  .dependsOn(sharedData)
  .settings(
    name := "descinet-data_l1",
    scalacOptions ++= List(
      "-Ymacro-annotations",
      "-Yrangepos",
      "-Wconf:cat=unused:silent", // Silence all unused warnings
      "-Wconf:cat=unused-nowarn:silent", // Silence unused warnings with @nowarn
      "-Wconf:msg=parameter value update in method validateModelId is never used:silent", // Silence specific unused parameter warning
      "-Wconf:cat=unused-imports:silent", // Silence unused import warnings
      "-Wconf:cat=unused-params:silent", // Silence unused parameter warnings
      "-language:reflectiveCalls"
    ),
    buildInfoKeys := Seq[BuildInfoKey](name, version, scalaVersion, sbtVersion),
    buildInfoPackage := "com.descinet.data_l1",
    resolvers += Resolver.mavenLocal,
    resolvers += Resolver.githubPackages("abankowski", "http-request-signer"),
    Defaults.itSettings,
    libraryDependencies ++= Seq(
      CompilerPlugin.kindProjector,
      CompilerPlugin.betterMonadicFor,
      CompilerPlugin.semanticDB,
      Libraries.tessellationCurrencyL1
    )
  )
