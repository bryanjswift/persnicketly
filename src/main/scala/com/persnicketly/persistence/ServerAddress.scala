package com.persnicketly.persistence

import com.mongodb.{ServerAddress => MongoDBAddress}

case class ServerAddress(host: String, port: Int) {
  val mongo = new MongoDBAddress(host, port)
}
