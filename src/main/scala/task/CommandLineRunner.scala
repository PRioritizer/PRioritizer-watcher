package task

import pullrequest.PullRequest
import sys.process._

class CommandLineRunner(repositories: String, command: String) extends TaskRunner {
  def run(pullRequest: PullRequest): Boolean = {
    val taskCommand = parseCommand(pullRequest)
    println(s"Executing: $taskCommand")
    taskCommand.! == 0
  }

  def runWithOutput(pullRequest: PullRequest): String = {
    val taskCommand = parseCommand(pullRequest)
    println(s"Executing: $taskCommand")
    taskCommand.!!
  }

  private def parseCommand(pullRequest: PullRequest): String = {
    val base = pullRequest.base
    command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", repositories)
  }
}
