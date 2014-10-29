package queue

import org.joda.time.DateTime

case class Message(timestamp: DateTime, contents: String)
