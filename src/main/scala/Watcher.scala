import events.EventDatabase
import queue.PullRequestQueue
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
      println("exit")
      queue.close()
    }
  }
}

object RabbitMQSettings {
  lazy val host = Settings.get("rabbitmq.host").getOrElse("localhost")
  lazy val username = Settings.get("rabbitmq.username").getOrElse("")
  lazy val password = Settings.get("rabbitmq.password").getOrElse("")
  lazy val queue = Settings.get("rabbitmq.queue").getOrElse("")
}

object MongoDBSettings {
  lazy val host = Settings.get("mongodb.host").getOrElse("localhost")
  lazy val port = Settings.get("mongodb.port").fold(27017)(p => p.toInt)
  lazy val username = Settings.get("mongodb.username").getOrElse("")
  lazy val password = Settings.get("mongodb.password").getOrElse("")
  lazy val database = Settings.get("mongodb.database").getOrElse("")
  lazy val collection = Settings.get("mongodb.collection").getOrElse("")
}

object TaskSettings {
  lazy val repositories = Settings.get("prioritizer.repositories").getOrElse("")
  lazy val command = Settings.get("prioritizer.command").getOrElse("")
}
