package queue

import com.rabbitmq.client.QueueingConsumer.Delivery
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}

class PullRequestQueue(host: String, username: String, password: String, queue: String) {
  private var connection: Connection = _
  private var channel: Channel = _

  def open(): Unit = {
    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setUsername(username)
    factory.setPassword(password)
    connection = factory.newConnection
  }

  def listen(action: (String => Boolean)): Unit = {
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
      val result = action(eventId)
      acknowledge(delivery, result)
    }
  }

  private def acknowledge(delivery: Delivery, positive: Boolean): Unit = {
    val acknowledgeMultiple = false
    val requeueMessage = false
    if (positive)
      channel.basicAck(delivery.getEnvelope.getDeliveryTag, acknowledgeMultiple)
    else
      channel.basicNack(delivery.getEnvelope.getDeliveryTag, acknowledgeMultiple, requeueMessage)
  }

  def close(): Unit = {
    if (channel != null && channel.isOpen)
      channel.close()

    if (connection != null && connection.isOpen)
      connection.close()
  }
}
