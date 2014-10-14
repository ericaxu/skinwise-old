name := """skinwise"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.32",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

javaOptions ++= Seq("-Xms512M", "-Xmx2G",
  "-XX:+CMSClassUnloadingEnabled", "-XX:+CMSPermGenSweepingEnabled",
  "-XX:MaxMetaspaceSize=512m")

scalacOptions ++= Seq("-feature")

includeFilter in(Assets, LessKeys.less) := "*.less"