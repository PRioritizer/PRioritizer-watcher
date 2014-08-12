package events

import pullrequest.PullRequest

case class Event(action: String, pullRequest: PullRequest)
