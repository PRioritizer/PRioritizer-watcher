import events.{MongoDatabase, EventDatabase}
import org.slf4j.LoggerFactory
import queue.{RabbitMQ, PullRequestQueue}
import settings.{TaskSettings, MongoDBSettings, RabbitMQSettings}
import task.{CommandLineRunner, TaskRunner}
import scala.util.{Failure, Success}

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
      TaskSettings.command
    )

    watch(queue, database, runner)
    println("Bye")
  }

  def watch(queue: PullRequestQueue, database: EventDatabase, runner: TaskRunner): Unit = {
    try {
      queue.open()
      database.open()

      queue listen { eventId =>
        logger info s"New event - ID: $eventId"

        database.getPullRequest(eventId) match {
          case Success(pr) =>
            logger info s"Database lookup - Repository: ${pr.base.owner}/${pr.base.repository}"
            logger info s"Prioritizing - Start process"
            val (result, output) = runner.run(pr)
            if (result)
              logger info s"Prioritizing - Process completed"
            else
              logger error s"Prioritizing - Process completed with errors"
            logger info s"Output - Begin\n${output.trim}"
            logger info s"Output - End"
            result
          case Failure(e) =>
            val stackTrace = e.getStackTrace.mkString("", "\n", "").trim
            logger error s"Prioritizing - Error: ${e.getMessage}"
            logger error s"Stack trace - Begin\n$stackTrace"
            logger error s"Stack trace - End"
            false
        }
      }
    } finally {
      queue.close()
    }
  }
}
