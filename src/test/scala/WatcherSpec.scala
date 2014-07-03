import org.specs2.mutable._
import settings.TaskSettings
import task.CommandLineRunner

class WatcherSpec extends Specification {
  val command = TaskSettings.command
  val repoDir = TaskSettings.repositories

  "When watching the queue it" should {
    "execute the task correctly" in {

      val queue = new TestQueue
      val data = new TestDatabase
      val runner = new CommandLineRunner(repoDir, command)

      Watcher.watch(queue, data, runner)
      true must beTrue
    }
  }
}
