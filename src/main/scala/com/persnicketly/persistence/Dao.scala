package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.{Logging, Persnicketly}

import Persnicketly.Config

trait Dao extends Logging {
  import Connection.mongo

  def collectionName: String

  lazy val collection = mongo(Config("db.name").or("persnicketly_test"))(collectionName)

  RegisterJodaTimeConversionHelpers()
}
