import queue.PullRequestQueue

object Watcher {
  def main(args: Array[String]): Unit = {
    val queue = new PullRequestQueue(
      RabbitMQSettings.host,
      RabbitMQSettings.username,
      RabbitMQSettings.password,
      RabbitMQSettings.queue)

    queue.open()

    try {
      queue listen { id =>
        println(s"> Event id: $id")
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
