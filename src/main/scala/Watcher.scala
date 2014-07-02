import queue.PullRequestQueue

object Watcher {
  def main(args: Array[String]): Unit = {
    val host = Settings.get("rabbitmq.host").get
    val username = Settings.get("rabbitmq.username").get
    val password = Settings.get("rabbitmq.password").get
    val queueName = Settings.get("rabbitmq.queue").get

    val queue = new PullRequestQueue(host, username, password, queueName)
    queue.open()

    queue listen { id =>
      println(s"> Event id: $id")
    }

    println("exit")
    queue.close()
  }
}
