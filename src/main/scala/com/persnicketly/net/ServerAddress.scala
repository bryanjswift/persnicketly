package com.persnicketly.net

import com.mongodb.{ServerAddress => MongoDBAddress}

case class ServerAddress(host: String, port: Int) {
  val mongo = new MongoDBAddress(host, port)
}
