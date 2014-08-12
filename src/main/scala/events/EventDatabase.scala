package events

import scala.util.Try

trait EventDatabase {
  def open(): Unit

  def getPullRequest(id: String) : Try[Event]

  def close(): Unit
}
