package com.persnicketly.net

import com.mongodb.{ServerAddress => MongoDBAddress}
import com.persnicketly.redis.Redis

case class ServerAddress(host: String, port: Int) {
  val mongo = new MongoDBAddress(host, port)
  val redis = new Redis(host, port)
}
