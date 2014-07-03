package task

import pullrequest.PullRequest
import sys.process._

class TaskRunner(repositories: String, command: String) {
  def run(pullRequest: PullRequest): Boolean = {
    val base = pullRequest.base
    val taskCommand = command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", repositories)

    println(s"Executing: $taskCommand")
    val result = taskCommand.!

    result == 0
  }
}
