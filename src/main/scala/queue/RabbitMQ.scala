package queue

import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}

class RabbitMQ(host: String, username: String, password: String, queue: String) extends PullRequestQueue {
  private var connection: Connection = _
  private var channel: Channel = _

  def open(): Unit = {
    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(username)
    factory.setPassword(password)
    connection = factory.newConnection
  }

  def listen(action: (Message => Unit)): Unit = {
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
      val message = new RabbitMessage(eventId, channel, delivery)
      val result = action(message)
    }
  }

  def close(): Unit = {
    if (channel != null && channel.isOpen)
      channel.close()

    if (connection != null && connection.isOpen)
      connection.close()
  }
}