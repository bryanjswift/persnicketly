package com.persnicketly.mill

import com.rabbitmq.client.AMQP

case class QueueConfig(
  host: String,
  port: Int,
  durable: Boolean,
  exclusive: Boolean,
  autodelete: Boolean,
  prefetch: Int,
  message: MessageConfig)

case class MessageConfig(contentType: String, persistent: Boolean, priority: Int) {
  val properties = {
    val builder = new AMQP.BasicProperties.Builder()
    builder.contentType(contentType)
    if (persistent) {
      builder.deliveryMode(2)
    } else {
      builder.deliveryMode(1)
    }
    builder.priority(priority)
    builder.build
  }
}
