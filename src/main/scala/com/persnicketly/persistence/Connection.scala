package com.persnicketly.persistence

import com.mongodb.{ServerAddress => MongoDBAddress}
import com.mongodb.casbah.Imports._
import com.persnicketly.net.ServerAddress
import com.persnicketly.Persnicketly.Config

object Connection {
  private val addresses = Config("db.hosts").or(List(ServerAddress("localhost", 27017)))
  lazy val mongo = Connection(addresses.map(_.mongo))

  def apply(hosts: List[MongoDBAddress]) = {
    if (hosts.length == 1) { MongoConnection(hosts.head) }
    else { MongoConnection(hosts) }
  }
}
