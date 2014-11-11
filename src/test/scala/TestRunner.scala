import events.Event
import task.TaskRunner

class TestRunner(command: String) extends TaskRunner {
  def run(event: Event): Boolean = {
    val taskCommand = parseCommand(event)
    println(s"Executing: $taskCommand")
    true
  }

  def canRunInfo(event: Event): (Boolean, String) = {
    (true, "<message>")
  }

  private def parseCommand(event: Event): String = {
    val base = event.pullRequest.base
    command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", "<dir>")
  }
}
