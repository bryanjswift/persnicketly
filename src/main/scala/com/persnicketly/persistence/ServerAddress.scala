package com.persnicketly.persistence

import com.mongodb.{ServerAddress => MongoDBAddress}
import com.rabbitmq.client.{Address => RabbitMQAddress}

case class ServerAddress(host: String, port: Int) {
  val mongo = new MongoDBAddress(host, port)
  val rabbit = new RabbitMQAddress(host, port)
}
