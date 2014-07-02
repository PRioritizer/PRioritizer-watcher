package queue

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}

class PullRequestQueue(host: String, username: String, password: String, queue: String) {
  var connection: Connection = _
  var channel: Channel = _

  def open(): Unit = {
    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(username)
    factory.setPassword(password)
    connection = factory.newConnection
  }

  def listen(action: (String => Unit)): Unit = {
    if (connection == null)
      throw new Exception("No connection")

    channel = connection.createChannel
    channel.queueDeclare(queue, true, false, false, null)
    val consumer = new QueueingConsumer(channel)
    channel.basicConsume(queue, false, consumer)
    channel.basicQos(1)

    while (true) {
      // Wait for next message
      val delivery = consumer.nextDelivery()
      val eventId = new String(delivery.getBody)
      action(eventId)
    }
  }

  def close(): Unit = {
    if (channel != null && channel.isOpen)
      channel.close()

    if (connection != null && connection.isOpen)
      connection.close()
  }
}
