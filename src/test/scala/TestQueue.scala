import queue.{Message, PullRequestQueue}

class TestQueue extends PullRequestQueue {
  def open(): Unit = {}

  def stream: Stream[Message] = {
    val list = List(
      Message("1"),
      Message("2"),
      Message("3"),
      Message("4"),
      Message("5")
    )

    list.toStream
  }

  def close(): Unit = {}
}