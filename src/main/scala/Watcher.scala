import java.io.PrintWriter

import events.{Event, EventDatabase, MongoDatabase}
import org.joda.time.DateTimeZone
import org.slf4j.LoggerFactory
import queue.{PullRequestQueue, RabbitMQ}
import settings._
import task.{CommandLineRunner, TaskRunner}
import utils.Extensions._

import scala.util.{Failure, Success}

object Watcher {
  val logger = LoggerFactory.getLogger("Watcher")

  def main(args: Array[String]): Unit = {
    val queue = new RabbitMQ(
      RabbitMQSettings.host,
      RabbitMQSettings.port,
      RabbitMQSettings.username,
      RabbitMQSettings.password,
      RabbitMQSettings.queue)

    val database = new MongoDatabase(
      MongoDBSettings.host,
      MongoDBSettings.port,
      MongoDBSettings.username,
      MongoDBSettings.password,
      MongoDBSettings.database,
      MongoDBSettings.collection)

    val runner = new CommandLineRunner(
      TaskSettings.repositories,
      TaskSettings.command,
      new PrintWriter(System.out, true)
    )

    while (true) {
      watch(queue, database, runner)
      logger info s"Connection - Trying to reconnect in ${WatcherSettings.connectionTimeout} seconds..."
      Thread.sleep(WatcherSettings.connectionTimeout * 1000)
    }

    println("Bye")
  }

  def watch(queue: PullRequestQueue, database: EventDatabase, runner: TaskRunner): Unit = {
    try {
      queue.open()
      database.open()

      queue.stream
      .map { m =>
        logger info s"New event - ID: ${m.contents}"
        database.getPullRequest(m.contents)
      }
      .foreach {
        case Event(timestamp, action, pr) =>
          logger info s"Database - Timestamp: ${timestamp.withZone(DateTimeZone.getDefault).toString("yyyy-MM-dd HH:mm:ss.SSS")}"
          logger info s"Database - Action: $action"
          logger info s"Database - Number: ${pr.number}"
          logger info s"Database - Repository: ${pr.base.owner}/${pr.base.repository}"

          if (!runner.canRun(pr)) {
            val (_, log) = runner.canRunInfo(pr)
            logger warn s"Skip - $log"
          } else {
            logger info s"Prioritizing - Start process"
            val result = runner.run(pr)
            if (result) logger info s"Prioritizing - Process completed"
            else logger error s"Prioritizing - Process completed with errors"
          }
      }
    } catch {
      case e: Throwable =>
        logger error s"Exception - ${e.getMessage}"
        logger error s"Stack trace - Begin\n${e.stackTraceToString}"
        logger error s"Stack trace - End"
    } finally {
      database.close()
      queue.close()
    }
  }
}
