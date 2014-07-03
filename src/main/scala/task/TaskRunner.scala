package task

import pullrequest.PullRequest

trait TaskRunner {
  def run(pullRequest: PullRequest): Boolean

  def runWithOutput(pullRequest: PullRequest): String
}
