package events

import com.mongodb._
import pullrequest.{Base, Head, PullRequest}

import scala.util.Try

class MongoDatabase(host: String, port: Int, username: String, password: String, databaseName: String, collectionName: String) extends EventDatabase {
  private var client: MongoClient = _
  private var database: DB = _
  private var collection: DBCollection = _

  def open(): Unit = {
    val server = new ServerAddress(host, port)
    client = if (username != null && username.nonEmpty) {
      val credential = MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray)
      new MongoClient(server, java.util.Arrays.asList(credential))
    } else {
      new MongoClient(server)
    }

    client.setReadPreference(ReadPreference.secondaryPreferred())
    database = client.getDB(databaseName)
    collection = database.getCollection(collectionName)
  }

  def getPullRequest(id: String) : Try[Event] = {
    Try {
      val query = new BasicDBObject("id", id)
      val fields = new BasicDBObject()

      fields.put("payload.action", 1)

      fields.put("payload.pull_request.head.label", 1)
      fields.put("payload.pull_request.head.sha", 1)
      fields.put("payload.pull_request.head.repo.name", 1)
      fields.put("payload.pull_request.head.repo.owner.login", 1)

      fields.put("payload.pull_request.base.label", 1)
      fields.put("payload.pull_request.base.sha", 1)
      fields.put("payload.pull_request.base.repo.name", 1)
      fields.put("payload.pull_request.base.repo.owner.login", 1)

      val result = collection.findOne(query, fields)

      val action = getField(result, "payload.action")

      val head_label = getField(result, "payload.pull_request.head.label")
      val head_sha = getField(result, "payload.pull_request.head.sha")
      val head_repo_name = getField(result, "payload.pull_request.head.repo.name", "Unknown")
      val head_repo_owner_login = getField(result, "payload.pull_request.head.repo.owner.login", "Unknown")

      val base_label = getField(result, "payload.pull_request.base.label")
      val base_sha = getField(result, "payload.pull_request.base.sha")
      val base_repo_name = getField(result, "payload.pull_request.base.repo.name")
      val base_repo_owner_login = getField(result, "payload.pull_request.base.repo.owner.login")

      Event(
        action,
        PullRequest(
          Head(head_label, head_sha, head_repo_owner_login, head_repo_name),
          Base(base_label, base_sha, base_repo_owner_login, base_repo_name)
        )
      )
    }
  }

  private def getField(obj: DBObject, fullPath: String, defaultValue: String = null): String = {
    def iter(x: AnyRef, path: Array[String]): String = {
      x match {
        case o: DBObject => iter(o.get(path.head), path.tail)
        case s: String => s
        case _ if defaultValue != null => defaultValue
        case _ => throw new NoSuchElementException(s"Unknown field $fullPath")
      }
    }
    iter(obj, fullPath.split("""\."""))
  }

  def close(): Unit = {
    if (client != null)
      client.close()
  }
}
