import org.joda.time.DateTime
import queue.{Message, PullRequestQueue}

class TestQueue extends PullRequestQueue {
  def open(): Unit = {}

  def stream: Stream[Message] = {
    val list = List(
      Message(DateTime.now.minusMinutes(5), "2251766881"),
      Message(DateTime.now.minusMinutes(4), "2251766799"),
      Message(DateTime.now.minusMinutes(3), "2251766932"),
      Message(DateTime.now.minusMinutes(2), "2251767277"),
      Message(DateTime.now.minusMinutes(1), "2251767227")
    )

    list.toStream
  }

  def close(): Unit = {}
}