package queue

import com.rabbitmq.client.QueueingConsumer.Delivery
import com.rabbitmq.client.Channel

class RabbitMessage(val contents: String, channel: Channel, delivery: Delivery) extends Message {
  override def acknowledge(success: Boolean): Unit = {
    val acknowledgeMultiple = false
    val requeueMessage = false
    if (success)
      channel.basicAck(delivery.getEnvelope.getDeliveryTag, acknowledgeMultiple)
    else
      channel.basicNack(delivery.getEnvelope.getDeliveryTag, acknowledgeMultiple, requeueMessage)
  }
}
