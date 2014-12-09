package settings

import java.io.{BufferedReader, FileNotFoundException, InputStreamReader}
import java.util.Properties

import scala.collection.JavaConverters._

object WatcherSettings {
  lazy val connectionTimeout = Settings.get("connection.timeout").fold(60)(p => p.toInt)
}

object RabbitMQSettings {
  lazy val host = Settings.get("rabbitmq.host").getOrElse("localhost")
  lazy val port = Settings.get("rabbitmq.port").fold(5672)(p => p.toInt)
  lazy val username = Settings.get("rabbitmq.username").getOrElse("")
  lazy val password = Settings.get("rabbitmq.password").getOrElse("")
  lazy val queue = Settings.get("rabbitmq.queue").getOrElse("")
}

object MongoDBSettings {
  lazy val host = Settings.get("mongodb.host").getOrElse("localhost")
  lazy val port = Settings.get("mongodb.port").fold(27017)(p => p.toInt)
  lazy val username = Settings.get("mongodb.username").getOrElse("")
  lazy val password = Settings.get("mongodb.password").getOrElse("")
  lazy val database = Settings.get("mongodb.database").getOrElse("")
  lazy val collection = Settings.get("mongodb.collection").getOrElse("")
}

object TaskSettings {
  lazy val output = Settings.get("prioritizer.output").getOrElse("")
  lazy val repositories = Settings.get("prioritizer.repositories").getOrElse("")
  lazy val command = Settings.get("prioritizer.command").getOrElse("")
}

object Settings {
  val fileName = "settings.properties"
  val resource = getClass.getResourceAsStream("/" + fileName)
  val data = read

  /**
   * @param property The name of the property.
   * @return True iff there exists a property with the give name.
   */
  def has(property: String): Boolean =
    data.get(property).isDefined

  /**
   * @param property The name of the property.
   * @return The value of the property.
   */
  def get(property: String): Option[String] =
    data.get(property)

  /**
   * Read the properties from the config file.
   * @return A map with the properties.
   */
  private def read: Map[String, String] = {
    if (resource == null)
      throw new FileNotFoundException(
        s"The configuration file was not found. Please make sure you copied $fileName.dist to $fileName.")

    // Read properties file
    val reader = new BufferedReader(new InputStreamReader(resource, java.nio.charset.StandardCharsets.UTF_8))
    val props = new Properties
    props.load(reader)
    props.readSystemOverride()
    props.asScala.toMap
  }

  implicit class RichProperties(properties: Properties) {
    def readSystemOverride(): Unit = {
      val keys = properties.keySet().asScala.collect({ case str: String => str })

      keys.foreach(key => {
        val propOverride = System.getProperty(key)
        if (propOverride != null)
          properties.put(key, propOverride)
      })
    }
  }
}
