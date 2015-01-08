import AssemblyKeys._

assemblySettings

name := "watcher"

version := "1.0"

scalaVersion := "2.11.0"

resolvers ++= Seq(
  "RoundEights" at "http://maven.spikemark.net/roundeights"
)

libraryDependencies ++= Seq(
  "com.github.nscala-time" %% "nscala-time" % "1.2.0",
  "com.rabbitmq" % "amqp-client" % "3.3.4",
  "org.mongodb" %% "casbah" % "2.7.2",
  "org.specs2" % "specs2_2.11" % "2.3.12" % "test",
  "com.roundeights" %% "hasher" % "1.0.0",
  "org.slf4j" % "slf4j-api" % "1.7.5",
  "org.slf4j" % "slf4j-simple" % "1.7.5"
)

// Skip WatcherSpec as it is more like an integration test that only runs in a proper environment
testOptions := Seq(Tests.Filter(s => s != "WatcherSpec"))
