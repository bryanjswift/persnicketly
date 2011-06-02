package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.{Logging, Persnicketly}

trait Dao extends Logging {
  import Persnicketly.Config
  RegisterJodaTimeConversionHelpers()

  def collectionName: String

  private val addresses = Config("db.hosts").or(List(ServerAddress("localhost", 27017)))
  lazy val connection = Connection(addresses.map(_.mongo))
  lazy val collection = connection(Config("db.name").or("persnicketly_test"))(collectionName)
}
