import pullrequest.PullRequest
import task.TaskRunner

class TestRunner(command: String) extends TaskRunner {
  def run(pullRequest: PullRequest): (Boolean, String) = {
    val taskCommand = parseCommand(pullRequest)
    println(s"Executing: $taskCommand")
    (true, "<output>")
  }

  private def parseCommand(pullRequest: PullRequest): String = {
    val base = pullRequest.base
    command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", "<dir>")
  }
}
