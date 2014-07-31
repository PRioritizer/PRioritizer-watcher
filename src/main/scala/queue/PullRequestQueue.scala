package queue

trait PullRequestQueue {
  def open(): Unit
  def stream: Stream[Message]
  def close(): Unit
}
