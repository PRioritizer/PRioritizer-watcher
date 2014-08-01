package task

import pullrequest.PullRequest

trait TaskRunner {
  def run(pullRequest: PullRequest): Boolean

  def canRun(pullRequest: PullRequest): Boolean = canRunInfo(pullRequest)._1

  def canRunInfo(pullRequest: PullRequest): (Boolean, String)
}
