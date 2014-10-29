package events

import org.joda.time.DateTime
import pullrequest.PullRequest

case class Event(timestamp: DateTime, action: String, pullRequest: PullRequest)
