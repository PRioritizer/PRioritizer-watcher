package task

import pullrequest.PullRequest

trait TaskRunner {
  def run(pullRequest: PullRequest): (Boolean, String)
}
