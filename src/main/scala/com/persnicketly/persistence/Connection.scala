package com.persnicketly.persistence

import com.mongodb.{ServerAddress => MongoDBAddress}
import com.mongodb.casbah.Imports._

object Connection {
  def apply(hosts: List[MongoDBAddress]) = {
    if (hosts.length == 1) { MongoConnection(hosts.head) }
    else { MongoConnection(hosts) }
  }
}
