import queue.PullRequestQueue

class TestQueue extends PullRequestQueue {
  def open(): Unit = {}

  def listen(action: (String => Boolean)): Unit = {
    val list = List(1,2,3,4,5)

    list.foreach { eventId =>
      action(s"$eventId")
    }
  }

  def close(): Unit = {}
}