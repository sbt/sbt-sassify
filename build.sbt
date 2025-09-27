lazy val sbtSassify = project
  .in(file("."))
  .enablePlugins(SbtPlugin)
  .enablePlugins(AutomateHeaderPlugin)
  .enablePlugins(ScriptedPlugin)

name := "sbt-sassify"
organization := "org.irundaia.sbt"
organizationName := "Han van Venrooij"
startYear := Some(2018)

javaOptions += "-Djna.nosys=true"

addSbtPlugin("com.typesafe.sbt" % "sbt-web" % "1.4.4")

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.11" % "test",
  "org.scalatest" %% "scalatest-mustmatchers" % "3.2.11" % "test",
  "org.scalatest" %% "scalatest-funspec" % "3.2.11" % "test",
  "net.java.dev.jna" % "jna" % "5.10.0"
)

// Compiler settings
sourcesInBase := false
crossPaths := false
scalacOptions ++= Seq(
  "-unchecked",
  "-Xlint",
  "-deprecation",
  "-feature",
  "-encoding",
  "UTF-8"
)
javacOptions ++= Seq("-source", "1.8", "-target", "1.8", "-Xlint")

pluginCrossBuild / sbtVersion := {
  scalaBinaryVersion.value match {
    case "2.12" =>
      "1.5.8"
    case _ =>
      "2.0.0-RC5"
  }
}
scriptedSbt := {
  scalaBinaryVersion.value match {
    case "2.12" => "1.11.6"
    case _      => (pluginCrossBuild / sbtVersion).value
  }
}
publishMavenStyle := false
licenses += License.Apache2

// Scalastyle settings
lazy val testScalastyle = taskKey[Unit]("testScalastyle")
testScalastyle := (Compile / scalastyle).toTask("").value
scalastyleFailOnError := true

// Scripted settings
scriptedBufferLog := false
scriptedLaunchOpts += "-Dplugin.version=" + version.value

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("+ test", "+ scripted")))
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(
    RefPredicate.StartsWith(Ref.Tag("v")),
    RefPredicate.Equals(Ref.Branch("main"))
  )
ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    commands = List("ci-release"),
    name = Some("Publish project"),
    env = Map(
      "PGP_PASSPHRASE" -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET" -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)
