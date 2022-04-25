ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.8"

libraryDependencies += "com.lihaoyi" %% "cask" % "0.8.0"
libraryDependencies += "org.wvlet.airframe" %% "airframe-log" % "22.3.0"
libraryDependencies += "com.lihaoyi" %% "os-lib" % "0.8.1"
libraryDependencies += "com.lihaoyi" %% "scalatags" % "0.11.1"
libraryDependencies += "net.ruippeixotog" %% "scala-scraper" % "2.2.1"
libraryDependencies += "com.lihaoyi" %% "requests" % "0.7.0"
libraryDependencies += "org.json4s" %% "json4s-native" % "4.0.5"

lazy val root = (project in file("."))
  .settings(
    name := "filmweb-scrapper"
  )
