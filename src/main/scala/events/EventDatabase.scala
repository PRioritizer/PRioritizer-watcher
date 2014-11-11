package events

trait EventDatabase {
  def open(): Unit

  def getPullRequest(id: String) : Event

  def close(): Unit
}
