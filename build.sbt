import Dependencies._

ThisBuild / scalaVersion     := "2.13.2"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "org.pfcoperez"
ThisBuild / sbtVersion       := "1.3.13"

lazy val root = (project in file("."))
  .settings(
    name := "TheButlerDidIt",
    libraryDependencies += scalaTest % Test,
    libraryDependencies += fastParse
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
