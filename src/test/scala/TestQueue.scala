import queue.{Message, PullRequestQueue}

class TestQueue extends PullRequestQueue {
  def open(): Unit = {}

  def listen(action: (Message => Unit)): Unit = {
    val list = List(
      TestMessage("1"),
      TestMessage("2"),
      TestMessage("3"),
      TestMessage("4"),
      TestMessage("5")
    )

    list.foreach { message =>
      action(message)
    }
  }

  def close(): Unit = {}
}