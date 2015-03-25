PRioritizer watcher
===================

[![Build Status](https://travis-ci.org/PRioritizer/PRioritizer-watcher.svg)](https://travis-ci.org/PRioritizer/PRioritizer-watcher)

A pull request watcher written in Scala.

The watcher listens on a [RabbitMQ](https://www.rabbitmq.com/) queue for messages that contain event ids of pull request events.
For each event the owner and repository info is fetched from a MongoDB database.
If the server contains a local clone of the repository, the pull requests are prioritized using the [analyzer](https://github.com/PRioritizer/PRioritizer-analyzer).

*Please note* that the watcher is specifically written for the [GHTorrent](http://ghtorrent.org/) project.

Prerequisites
-------------

* The [analyzer](https://github.com/PRioritizer/PRioritizer-analyzer)
* [Scala](http://www.scala-lang.org/) compiler
* [JVM 8](https://java.com/download/)

Building
--------

1. Clone the project into `~/watcher`
2. Install dependencies and build the project with `sbt compile`
3. Copy `src/main/resources/settings.properties.dist` to `src/main/resources/settings.properties`
4. Configure the application by editing `src/main/resources/settings.properties`
  * e.g. path to output: `~/json/`
  * e.g. path to repositories: `~/repos/`
  * e.g. path to analyzer: `~/analyzer/run $owner $repository $dir`
5. Package the project into a `.jar` file with `sbt assembly`

Running
-------

1. To run in the foreground: `./run`
2. To run in the background: `./prioritizer start`
  * To attach to the background process: `./prioritizer attach`
  * To stop the background process: `./prioritizer stop`
