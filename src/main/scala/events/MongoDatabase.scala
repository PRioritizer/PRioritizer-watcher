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

      fields.put("payload.pull_request.number", 1)

      fields.put("payload.pull_request.head.label", 1)
      fields.put("payload.pull_request.head.sha", 1)
      fields.put("payload.pull_request.head.repo.name", 1)
      fields.put("payload.pull_request.head.repo.owner.login", 1)

      fields.put("payload.pull_request.base.label", 1)
      fields.put("payload.pull_request.base.sha", 1)
      fields.put("payload.pull_request.base.repo.name", 1)
      fields.put("payload.pull_request.base.repo.owner.login", 1)

      val result = collection.findOne(query, fields)

      if (!getField(result, "payload.action").isDefined)
        throw new NoSuchElementException("The event could not be retrieved from the database")

      val action = getField[String](result, "payload.action").get

      val number = getField[Int](result, "payload.pull_request.number").get

      val head_label = getField[String](result, "payload.pull_request.head.label").get
      val head_sha = getField[String](result, "payload.pull_request.head.sha").get
      val head_repo_name = getField[String](result, "payload.pull_request.head.repo.name").getOrElse("Unknown")
      val head_repo_owner_login = getField[String](result, "payload.pull_request.head.repo.owner.login").getOrElse("Unknown")

      val base_label = getField[String](result, "payload.pull_request.base.label").get
      val base_sha = getField[String](result, "payload.pull_request.base.sha").get
      val base_repo_name = getField[String](result, "payload.pull_request.base.repo.name").get
      val base_repo_owner_login = getField[String](result, "payload.pull_request.base.repo.owner.login").get

      Event(
        action,
        PullRequest(
          number,
          Head(head_label, head_sha, head_repo_owner_login, head_repo_name),
          Base(base_label, base_sha, base_repo_owner_login, base_repo_name)
        )
      )
    }
  }

  private def getField[T](obj: DBObject, fullPath: String): Option[T] = {
    def iteration(x: Any, path: Array[String]): Option[T] = {
      x match {
        case l: BasicDBList => Some(l.toArray.toList.map(e => iteration(e, path)).asInstanceOf[T])
        case o: DBObject => iteration(o.get(path.head), path.tail)
        case s: String => Some(s.asInstanceOf[T])
        case i: Int => Some(i.asInstanceOf[T])
        case _ => None
      }
    }
    iteration(obj, fullPath.split("""\."""))
  }

  def close(): Unit = {
    if (client != null)
      client.close()
  }
}
