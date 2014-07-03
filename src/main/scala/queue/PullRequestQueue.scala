package queue

trait PullRequestQueue {
  def open(): Unit
  def listen(action: (String => Boolean)): Unit
  def close(): Unit
}
