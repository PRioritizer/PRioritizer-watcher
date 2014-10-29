import events.{Event, EventDatabase}
import org.joda.time.DateTime
import pullrequest.{Base, Head, PullRequest}

import scala.util.Try

class TestDatabase extends EventDatabase {
  override def open(): Unit = {}

  override def getPullRequest(id: String): Try[Event] = {
    Try {
      Event(
        DateTime.now.minusMinutes(1),
        "opened",
        PullRequest(
          42,
          Head(s"head: $id", "9b43dbe0cbc5dd5f32063ce613ecc8cf33dd39f8", "erikvdv1", "RxJava"),
          Base(s"base: $id", "dd39f8f32063ce613ecc8cf39b43dbe0cbc5dd53", "Netflix", "RxJava")
        )
      )
    }
  }

  override def close(): Unit = {}
}
