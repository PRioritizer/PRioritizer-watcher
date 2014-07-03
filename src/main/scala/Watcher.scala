import events.{MongoDatabase, EventDatabase}
import queue.{RabbitMQ, PullRequestQueue}
import settings.{TaskSettings, MongoDBSettings, RabbitMQSettings}
import task.{CommandLineRunner, TaskRunner}
import scala.util.{Failure, Success}

object Watcher {

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
        print(s"> Event id: $eventId")

        database.getPullRequest(eventId) match {
          case Success(pullReq) =>
            println(s" repo: ${pullReq.base.owner}/${pullReq.base.repository}")
            val result = runner.run(pullReq)
            result
          case Failure(e) =>
            println(s" Error - ${e.getMessage}")
            false
        }
      }
    } finally {
      queue.close()
    }
  }
}
