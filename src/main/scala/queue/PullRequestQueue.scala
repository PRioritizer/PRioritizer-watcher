package queue

trait PullRequestQueue {
  def open(): Unit
  def listen(action: (Message => Unit)): Unit
  def close(): Unit
}
