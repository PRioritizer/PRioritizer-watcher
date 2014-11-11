package events

import com.mongodb._
import com.mongodb.casbah.commons.MongoDBObject
import org.joda.time.DateTime
import pullrequest.{Base, Head, PullRequest}

import scala.util.Try

object PullRequestFields {
  val created = "created_at"
  val action = "payload.action"
  val number = "payload.pull_request.number"
  val headLabel = "payload.pull_request.head.label"
  val headSha = "payload.pull_request.head.sha"
  val headName = "payload.pull_request.head.repo.name"
  val headOwner = "payload.pull_request.head.repo.owner.login"
  val baseLabel = "payload.pull_request.base.label"
  val baseSha = "payload.pull_request.base.sha"
  val baseName = "payload.pull_request.base.repo.name"
  val baseOwner = "payload.pull_request.base.repo.owner.login"

  val select = List(
    created,
    action,
    number,
    headLabel,
    headSha,
    headName,
    headOwner,
    baseLabel,
    baseSha,
    baseName,
    baseOwner
  )
}

class MongoDatabase(host: String, port: Int, username: String, password: String, databaseName: String, collectionName: String) extends EventDatabase {
  private var client: MongoClient = _
  private var database: DB = _

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
  }

  def getPullRequest(id: String) : Event = {
    val result = getByKey(collectionName, List("id" -> id), PullRequestFields.select)

    if (!result.get(PullRequestFields.action).isDefined)
      throw new NoSuchElementException("The event could not be retrieved from the database")

    val timestamp = result.get(PullRequestFields.created).get.asInstanceOf[String]
    val action = result.get(PullRequestFields.action).get.asInstanceOf[String]
    val number = result.get(PullRequestFields.number).get.asInstanceOf[Int]

    val head_label = result.get(PullRequestFields.headLabel).get.asInstanceOf[String]
    val head_sha = result.get(PullRequestFields.headSha).get.asInstanceOf[String]
    val head_repo_name = result.getOrElse(PullRequestFields.headName, "Unknown").asInstanceOf[String]
    val head_repo_owner_login = result.getOrElse(PullRequestFields.headOwner, "Unknown").asInstanceOf[String]

    val base_label = result.get(PullRequestFields.baseLabel).get.asInstanceOf[String]
    val base_sha = result.get(PullRequestFields.baseSha).get.asInstanceOf[String]
    val base_repo_name = result.get(PullRequestFields.baseName).get.asInstanceOf[String]
    val base_repo_owner_login = result.get(PullRequestFields.baseOwner).get.asInstanceOf[String]

    Event(
      DateTime.parse(timestamp),
      action,
      PullRequest(
        number,
        Head(head_label, head_sha, head_repo_owner_login, head_repo_name),
        Base(base_label, base_sha, base_repo_owner_login, base_repo_name)
      )
    )
  }

  def getByKey(collectionName: String, key: List[(String, Any)], select: List[String]) : Map[String, Any] = {
    if (key.exists { case (k, v) => k == null || k == "" })
      return Map()

    val query = MongoDBObject(key)

    val fields = new BasicDBObject()
    select.foreach(f => fields.put(f, 1))

    val collection = database.getCollection(collectionName)
    val result = collection.findOne(query, fields)
    select
      .map(f => getField[Any](result, f).map(v => (f, v)))
      .flatten
      .toMap
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
