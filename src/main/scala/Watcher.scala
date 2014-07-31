import java.io.PrintWriter

import events.{MongoDatabase, EventDatabase}
import org.slf4j.LoggerFactory
import queue.{RabbitMQ, PullRequestQueue}
import settings.{TaskSettings, MongoDBSettings, RabbitMQSettings}
import task.{CommandLineRunner, TaskRunner}
import scala.util.{Failure, Success}
import utils.Extensions._

object Watcher {
  val logger = LoggerFactory.getLogger("Watcher")

  def main(args: Array[String]): Unit = {
    val queue = new RabbitMQ(
      RabbitMQSettings.host,
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

    watch(queue, database, runner)
    println("Bye")
  }

  def watch(queue: PullRequestQueue, database: EventDatabase, runner: TaskRunner): Unit = {
    try {
      queue.open()
      database.open()

      queue listen { message =>
        val eventId = message.contents
        logger info s"New event - ID: $eventId"

        database.getPullRequest(eventId) match {
          case Success(pr) =>
            logger info s"Database lookup - Repository: ${pr.base.owner}/${pr.base.repository}"

            val (canRun, log) = runner.canRun(pr)
            if (!canRun) {
              logger warn s"Skip - $log"
              message.acknowledge(success = false)
            }

            logger info s"Prioritizing - Start process"
            val result = runner.run(pr)
            if (result)
              logger info s"Prioritizing - Process completed"
            else
              logger error s"Prioritizing - Process completed with errors"
            result

          case Failure(e) =>
            logger error s"Database lookup - Error: ${e.getMessage}"
            logger error s"Stack trace - Begin\n${e.stackTraceToString}"
            logger error s"Stack trace - End"
            message.acknowledge(success = false)
        }
      }
    } finally {
      queue.close()
    }
  }
}
