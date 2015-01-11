package queue

import com.rabbitmq.client.QueueingConsumer.Delivery
import com.rabbitmq.client.{Channel, Connection, ConnectionFactory, QueueingConsumer}

class RabbitMQ(host: String, port: Int, username: String, password: String, queue: String) extends PullRequestQueue {
  private var connection: Connection = _
  private var channel: Channel = _
  private var _stream: Stream[Message] = _

  def open(): Unit = {
    val factory = new ConnectionFactory
    factory.setHost(host)
    factory.setPort(port)
    factory.setUsername(username)
    factory.setPassword(password)
    connection = factory.newConnection
  }

  def stream: Stream[Message] = {
    if (_stream == null) {
      val consumer = listen
      _stream = Stream continually wait(consumer)
    }
    _stream
  }

  def close(): Unit = {
    if (channel != null && channel.isOpen)
      channel.close()

    if (connection != null && connection.isOpen)
      connection.close()
  }

  private def listen: QueueingConsumer = {
    if (connection == null)
      throw new Exception("No connection")

    channel = connection.createChannel
    channel.queueDeclare(queue, true, false, false, null)
    val consumer = new QueueingConsumer(channel)
    channel.basicQos(1)
    channel.basicConsume(queue, false, consumer)
    consumer
  }

  private def wait(consumer: QueueingConsumer): Message = {
    // Wait for next message
    val delivery = consumer.nextDelivery()
    acknowledge(delivery)
    val eventId = new String(delivery.getBody)
    Message(eventId)
  }

  def acknowledge(delivery: Delivery, success: Boolean = true): Unit = {
    val acknowledgeMultiple = false
    val requeueMessage = false
    if (success)
      channel.basicAck(delivery.getEnvelope.getDeliveryTag, acknowledgeMultiple)
    else
      channel.basicNack(delivery.getEnvelope.getDeliveryTag, acknowledgeMultiple, requeueMessage)
  }
}
