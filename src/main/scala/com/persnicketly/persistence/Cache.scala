package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

object Cache extends Dao {
  val collectionName = "cache"

  def put(key: String, value: DateTime): Boolean = {
    val query = MongoDBObject("_id" -> key)
    val obj = MongoDBObject("_id" -> key, "value" -> value)
    collection.update(query, obj, upsert = true, multi = false)
    val result = get[DateTime](key)
    result.isDefined && result.get == value
  }

  def get[T](key: String)(implicit mf: Manifest[T]): Option[T] = {
    val query = MongoDBObject("_id" -> key)
    val obj = collection.findOne(query).get
    obj.getAs[T]("value")
  }
}
