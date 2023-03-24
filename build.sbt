ThisBuild / scalaVersion := "2.13.7"
ThisBuild / sbtVersion := "1.3.13"

lazy val root = (project in file("."))
  .aggregate(thebutlerdidit.js, thebutlerdidit.jvm)
  .settings(
    publish := {},
    publishLocal := {}
  )

lazy val thebutlerdidit = crossProject(JSPlatform, JVMPlatform)
  .in(file("."))
  .settings(
    name := "TheButlerDidIt",
    version := "0.1.0-SNAPSHOT",
    organization := "org.pfcoperez",
    libraryDependencies += "org.scalatest" %%% "scalatest" % "3.2.10" % Test,
    libraryDependencies += "com.lihaoyi" %%% "fastparse" % "3.0.1",
  ).
  jvmSettings(
    // Add JVM-specific settings here
  )
  .jsSettings(
    // Add JS-specific settings here
    libraryDependencies += "org.scala-js" %%% "scalajs-dom" % "2.0.0",
    scalaJSUseMainModuleInitializer := true
  )
