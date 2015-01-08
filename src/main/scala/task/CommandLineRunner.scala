package task

import java.io.{ByteArrayOutputStream, File, PrintWriter}

import events.Event
import org.joda.time.DateTime
import pullrequest.Base
import settings.TaskSettings

import scala.sys.process._

class CommandLineRunner(repositories: String, command: String, output: PrintWriter) extends TaskRunner {
  def runWithOutput(event: Event): (Boolean, String, String) = {
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)
    val taskCommand = parseCommand(event)
    val exitValue = taskCommand.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue == 0, stdout.toString, stderr.toString)
  }

  def run(event: Event): Boolean = {
    val logger: (String => Unit) = if (output != null) output.println else (s: String) => ()
    val taskCommand = parseCommand(event)
    val exitValue = taskCommand.!(ProcessLogger(logger, (s: String) => ()))
    exitValue == 0
  }

  def canRunInfo(event: Event): (Boolean, String) = {
    val pullRequest = event.pullRequest
    val base = pullRequest.base
    val repoFile = getRepoFile(base)
    val bareRepoFile = getBareRepoFile(base)

    if (!repoFile.exists && !bareRepoFile.exists)
      (false, "Repository directory does not exist")
    else if (event.timestamp.isBefore(getLatestUpdate(base)))
      (false, "Already up-to-date with respect to the event")
    else
      (true, "")
  }

  private def parseCommand(event: Event): String = {
    val pullRequest = event.pullRequest
    val base = pullRequest.base
    val repoFile = getRepoFile(base)
    val bareRepoFile = getBareRepoFile(base)
    val path = if (bareRepoFile.exists) bareRepoFile.getAbsolutePath else repoFile.getAbsolutePath

    command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", path)
  }

  private def getRepoFile(base: Base) =
    new File(new File(repositories, base.owner.toLowerCase), base.repository.toLowerCase)

  private def getBareRepoFile(base: Base) =
    new File(new File(repositories, base.owner.toLowerCase), base.repository.toLowerCase + ".git")

  private def getLatestUpdate(base: Base): DateTime = {
    val folder = new File(TaskSettings.output, base.owner)
    if (folder == null || !folder.exists) return new DateTime(0)
    val files = folder.listFiles.toList.filter(f => f.getName.startsWith(base.repository + '.'))
    val date = files.headOption.map(f => new DateTime(f.lastModified))
    date.getOrElse(new DateTime(0))
  }

}
