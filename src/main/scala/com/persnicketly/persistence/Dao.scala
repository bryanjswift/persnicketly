package com.persnicketly.persistence

import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.{Persnicketly, Logging}
import Persnicketly.Config
import com.yammer.metrics.Instrumented

trait Dao extends Logging with Instrumented {
  import Connection.mongo

  def collectionName: String

  lazy val collection = mongo(Config("db.name").or("persnicketly_test"))(collectionName)

  RegisterJodaTimeConversionHelpers()
}
