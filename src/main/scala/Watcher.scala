import events.EventDatabase
import queue.PullRequestQueue
import settings.{TaskSettings, MongoDBSettings, RabbitMQSettings, Settings}
import task.TaskRunner
import scala.util.{Failure, Success}

object Watcher {

  def main(args: Array[String]): Unit = {
    val queue = new PullRequestQueue(
      RabbitMQSettings.host,
      RabbitMQSettings.username,
      RabbitMQSettings.password,
      RabbitMQSettings.queue)

    val database = new EventDatabase(
      MongoDBSettings.host,
      MongoDBSettings.port,
      MongoDBSettings.username,
      MongoDBSettings.password,
      MongoDBSettings.database,
      MongoDBSettings.collection)

    val runner = new TaskRunner(
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
            print(s" base: ${pullReq.base.owner}/${pullReq.base.repository}")
            println(s" head: ${pullReq.head.owner}/${pullReq.head.repository}")
            val result = runner.run(pullReq)
          //            channel.basicAck(delivery.getEnvelope.getDeliveryTag, false)
          case Failure(e) =>
            println(s" Error - ${e.getMessage}")
          //            channel.basicNack(delivery.getEnvelope.getDeliveryTag, false, true)
        }
      }
    } finally {
      queue.close()
    }
  }
}
