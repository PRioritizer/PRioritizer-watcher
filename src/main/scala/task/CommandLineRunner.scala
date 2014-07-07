package task

import java.io.{PrintWriter, ByteArrayOutputStream, File}
import pullrequest.{Base, PullRequest}
import sys.process._

class CommandLineRunner(repositories: String, command: String) extends TaskRunner {
  def runWithExitCode(pullRequest: PullRequest): Boolean = {
    val taskCommand = parseCommand(pullRequest)
    taskCommand.! == 0
  }

  def runWithOutput(pullRequest: PullRequest): String = {
    val taskCommand = parseCommand(pullRequest)
    taskCommand.!!
  }

  def run(pullRequest: PullRequest): (Boolean, String) = {
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)
    val taskCommand = parseCommand(pullRequest)
    val exitValue = taskCommand.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue == 0, stdout.toString)
  }

  def canRun(pullRequest: PullRequest): (Boolean, String) = {
    val base = pullRequest.base
    val repoFile = getRepoFile(base)

    if (repoFile.exists)
      (true, "")
    else
      (false, "Repository directory does not exist")
  }

  private def parseCommand(pullRequest: PullRequest): String = {
    val base = pullRequest.base
    val path = getRepoFile(base).getAbsolutePath

    command
      .replace("$owner", base.owner)
      .replace("$repository", base.repository)
      .replace("$dir", path)
  }

  private def getRepoFile(base: Base) =
    new File(new File(repositories, base.owner), base.repository)

}
