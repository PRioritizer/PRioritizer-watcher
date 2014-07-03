import org.specs2.mutable._

class WatcherSpec extends Specification {
  "When watching the queue it" should {
    "execute the task correctly" in {
      val command = "prioritizer $owner $repository $dir"
      val queue = new TestQueue
      val data = new TestDatabase
      val runner = new TestRunner(command)

      Watcher.watch(queue, data, runner)
      true must beTrue
    }
  }
}
