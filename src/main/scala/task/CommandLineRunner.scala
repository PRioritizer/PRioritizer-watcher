package task

import java.io.{PrintWriter, ByteArrayOutputStream, File}
import pullrequest.{Base, PullRequest}
import sys.process._

class CommandLineRunner(repositories: String, command: String, output: PrintWriter) extends TaskRunner {
  def runWithOutput(pullRequest: PullRequest): (Boolean, String, String) = {
    val stdout = new ByteArrayOutputStream
    val stderr = new ByteArrayOutputStream
    val stdoutWriter = new PrintWriter(stdout)
    val stderrWriter = new PrintWriter(stderr)
    val taskCommand = parseCommand(pullRequest)
    val exitValue = taskCommand.!(ProcessLogger(stdoutWriter.println, stderrWriter.println))
    stdoutWriter.close()
    stderrWriter.close()
    (exitValue == 0, stdout.toString, stderr.toString)
  }

  def run(pullRequest: PullRequest): Boolean = {
    val logger: (String => Unit) = if (output != null) output.println else (s: String) => ()
    val taskCommand = parseCommand(pullRequest)
    val exitValue = taskCommand.!(ProcessLogger(logger, (s: String) => ()))
    exitValue == 0
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
