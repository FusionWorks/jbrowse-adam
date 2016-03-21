name := "jbrowse-adam"
organization := "md.fusionworks"

version := "0.1"

scalaVersion := "2.11.7"

//uncomment this to use `sbt dependencies`
/*
import play.PlayImport.PlayKeys._

lazy val pr = (project in file(".")).enablePlugins(PlayScala)
*/
scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= sprayDependencies ++ adamDependencies ++ sparkDependencies

mainClass in Compile := Some("md.fusionworks.adam.jbrowse.Boot")

lazy val sprayDependencies = {
  val sprayV = "1.3.3"
  Seq(
    "io.spray" %% "spray-can" % sprayV,
    "io.spray" %% "spray-routing" % sprayV,
    "io.spray" %% "spray-testkit" % sprayV % "test",
    "io.spray" %% "spray-json" % "1.3.2",
    "org.specs2" %% "specs2-core" % "2.3.11" % "test"
  )
}

lazy val adamDependencies = Seq("org.bdgenomics.adam" %% "adam-core" % "0.17.0" % "provided")


lazy val sparkDependencies = {
  val sparkV = "1.6.0"
  Seq(
    "org.apache.spark" %% "spark-core" % sparkV % "provided",
    "org.apache.spark" %% "spark-sql" % sparkV % "provided"
  )
}

Revolver.settings: Seq[sbt.Def.Setting[_]]

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith "reference.conf" => MergeStrategy.concat
  case PathList(ps@_*) if ps.last endsWith "_SUCCESS" => MergeStrategy.discard
  case PathList(ps@_*) if ps.last endsWith ".parquet" => MergeStrategy.discard
  case _ => MergeStrategy.first
}

run in Compile <<= Defaults.runTask(fullClasspath in Compile, mainClass in (Compile, run), runner in (Compile, run))
import spray.revolver.SbtCompatImpl.reStart
fullClasspath in reStart <<= fullClasspath in Compile

test in assembly := {}
