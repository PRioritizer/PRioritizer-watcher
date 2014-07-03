package events

import pullrequest.PullRequest
import scala.util.Try

trait EventDatabase {
  def open(): Unit

  def getPullRequest(id: String) : Try[PullRequest]

  def close(): Unit
}
