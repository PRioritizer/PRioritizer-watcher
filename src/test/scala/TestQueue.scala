import queue.{Message, PullRequestQueue}

class TestQueue extends PullRequestQueue {
  def open(): Unit = {}

  def stream: Stream[Message] = {
    val list = List(
      Message("2251766881"),
      Message("2251766799"),
      Message("2251766932"),
      Message("2251767277"),
      Message("2251767227")
    )

    list.toStream
  }

  def close(): Unit = {}
}