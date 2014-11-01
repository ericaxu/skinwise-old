name := """skinwise"""

version := "1.0-SNAPSHOT"

lazy val root = (project in file(".")).enablePlugins(PlayJava)

scalaVersion := "2.11.1"

libraryDependencies ++= Seq(
  "mysql" % "mysql-connector-java" % "5.1.32",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "net.sf.trove4j" % "trove4j" % "3.0.3",
  javaJdbc,
  javaEbean,
  cache,
  javaWs
)

dependencyOverrides ++= Set(
  "org.avaje.ebeanorm" % "avaje-ebeanorm-agent" % "4.1.6",
  "org.avaje.ebeanorm" % "avaje-ebeanorm" % "4.1.8"
)

javaOptions ++= Seq("-Xms512M", "-Xmx2G",  "-XX:+CMSClassUnloadingEnabled")

scalacOptions ++= Seq("-feature")

includeFilter in(Assets, LessKeys.less) := "*.less"