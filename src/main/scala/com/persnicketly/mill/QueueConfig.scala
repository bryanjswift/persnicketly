package com.persnicketly.mill

import com.rabbitmq.client.AMQP

case class QueueConfig(
  host: String,
  port: Int,
  name: String,
  durable: Boolean,
  exclusive: Boolean,
  autodelete: Boolean,
  prefetch: Int,
  message: MessageConfig)

case class MessageConfig(contentType: String, persistent: Boolean, priority: Int) {
  val properties = {
    val props = new AMQP.BasicProperties()
    props.setContentType(contentType)
    if (persistent) {
      props.setDeliveryMode(2)
    } else {
      props.setDeliveryMode(1)
    }
    props.setPriority(priority)
    props
  }
}
