package events

import pullrequest.PullRequest

case class Event(`type`: String, pullRequest: PullRequest)
