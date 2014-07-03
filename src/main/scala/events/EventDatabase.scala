package events

import com.mongodb._
import pullrequest.{Base, Head, PullRequest}
import scala.util.Try

class EventDatabase(host: String, port: Int, username: String, password: String, databaseName: String, collectionName: String) {
  private var client: MongoClient = _
  private var database: DB = _
  private var collection: DBCollection = _

  def open(): Unit = {
    val credential = MongoCredential.createMongoCRCredential(username, databaseName, password.toCharArray)
    val server = new ServerAddress(host, port)
    client = new MongoClient(server, java.util.Arrays.asList(credential))
    database = client.getDB(databaseName)
    collection = database.getCollection(collectionName)
  }

  def getPullRequest(id: String) : Try[PullRequest] = {
    Try {
      val query = new BasicDBObject("id", id)
      val fields = new BasicDBObject()

      fields.put("payload.pull_request.head.label", 1)
      fields.put("payload.pull_request.head.sha", 1)
      fields.put("payload.pull_request.head.repo.name", 1)
      fields.put("payload.pull_request.head.repo.owner.login", 1)

      fields.put("payload.pull_request.base.label", 1)
      fields.put("payload.pull_request.base.sha", 1)
      fields.put("payload.pull_request.base.repo.name", 1)
      fields.put("payload.pull_request.base.repo.owner.login", 1)

      val result = collection.findOne(query, fields)

      val head_label = getField(result, "payload.pull_request.head.label")
      val head_sha = getField(result, "payload.pull_request.head.sha")
      val head_repo_name = getField(result, "payload.pull_request.head.repo.name")
      val head_repo_owner_login = getField(result, "payload.pull_request.head.repo.owner.login")

      val base_label = getField(result, "payload.pull_request.base.label")
      val base_sha = getField(result, "payload.pull_request.base.sha")
      val base_repo_name = getField(result, "payload.pull_request.base.repo.name")
      val base_repo_owner_login = getField(result, "payload.pull_request.base.repo.owner.login")

      PullRequest(
        Head(head_label, head_sha, head_repo_name, head_repo_owner_login),
        Base(base_label, base_sha, base_repo_name, base_repo_owner_login)
      )
    }
  }

  private def getField(obj: DBObject, path: String): String = {
    def iter(x: AnyRef, path: Array[String]): String = {
      x match {
        case o: DBObject => iter(o.get(path.head), path.tail)
        case s: String => s
      }
    }
    iter(obj, path.split("""\."""))
  }

  def close(): Unit = {
    if (client != null)
      client.close()
  }
}
