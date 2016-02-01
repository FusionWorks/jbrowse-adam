name := "jbrowse-adam"
organization := "md.fusionworks"

version := "0.1"

scalaVersion := "2.10.5"

//uncomment this to use `sbt dependencies`
/*
import play.PlayImport.PlayKeys._

lazy val pr = (project in file(".")).enablePlugins(PlayScala)
*/
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= sprayDependencies ++ adamDependencies ++ sparkDependencies

lazy val sprayDependencies = {
  val sprayV = "1.3.3"
  Seq(
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "io.spray" %% "spray-testkit" % sprayV % "test",
    "io.spray" %% "spray-json" % sprayV,
    "org.specs2" %% "specs2-core" % "2.3.11" % "test"
  )
}


lazy val adamDependencies = Seq("org.bdgenomics.adam" % "adam-core" % "0.16.0" % "provided")


lazy val sparkDependencies = {
  val sparkV = "1.6.0"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkV % "provided",
    "org.apache.spark" %% "spark-sql" % sparkV % "provided"
  )
}

Revolver.settings

//todo: try to clean this
assemblyMergeStrategy in assembly := {
  case PathList("git.properties") => MergeStrategy.discard
  case PathList("log4j.properties") => MergeStrategy.first
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith ".txt" => MergeStrategy.first
  case PathList(ps@_*) if ps.last endsWith "package-info.class" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith "plugin.properties" => MergeStrategy.concat
  case PathList(ps@_*) if ps.last endsWith "reference.conf" => MergeStrategy.concat
  case _ => MergeStrategy.first
}

test in assembly := {}
