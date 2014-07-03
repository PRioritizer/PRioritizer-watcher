import AssemblyKeys._

assemblySettings

name := "watcher"

version := "1.0"

scalaVersion := "2.11.0"

libraryDependencies ++= Seq(
  "com.rabbitmq" % "amqp-client" % "3.3.4",
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.specs2" % "specs2_2.11" % "2.3.12",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)
