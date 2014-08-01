import pullrequest.PullRequest
import task.TaskRunner

class TestRunner(command: String) extends TaskRunner {
  def run(pullRequest: PullRequest): Boolean = {
    val taskCommand = parseCommand(pullRequest)
    println(s"Executing: $taskCommand")
    true
  }

  def canRunInfo(pullRequest: PullRequest): (Boolean, String) = {
    (true, "<message>")
  }

  private def parseCommand(pullRequest: PullRequest): String = {
    val base = pullRequest.base
    command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", "<dir>")
  }
}
