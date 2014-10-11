name := """skinwise"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.32",
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

scalacOptions ++= Seq("-feature")

includeFilter in (Assets, LessKeys.less) := "*.less"