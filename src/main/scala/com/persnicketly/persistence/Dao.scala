package com.persnicketly.persistence

import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.{Persnicketly, Logging}
import Persnicketly.Config
import com.yammer.metrics.scala.Instrumented

trait Dao extends Logging with Instrumented {
  import Connection.mongo

  def collectionName: String

  lazy val db = mongo(Config("db.name").or("persnicketly_test"))
  lazy val collection = db(collectionName)

  RegisterJodaTimeConversionHelpers()
}
