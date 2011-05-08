package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.web.Persnicketly
import com.persnicketly.readability.model.User
import org.bson.types.ObjectId

class UserDao {
  import UserDao._
  RegisterJodaTimeConversionHelpers()
  val config = Persnicketly.Config
  val connection = MongoConnection(config("db.host").or("localhost"), config("db.port").or(27017))
  val users = connection(config("db.name").or("persnicketly_test"))("users")
  def save(user: User): Option[ObjectId] = {
    users.save(user)
    users.findOne(MongoDBObject("request_token_value" -> user.requestToken.value)).get._id
  }
}

object UserDao {
  implicit def user2dbobject(user: User): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "request_token_value" -> user.requestToken.value
    builder += "request_token_secret" -> user.requestToken.secret
    if (user.accessToken.isDefined) {
      builder += "access_token_value" -> user.accessToken.get.value
      builder += "access_token_secret" -> user.accessToken.get.secret
    }
    if (user.verifier.isDefined) {
      builder += "verifier" -> user.verifier.get
    }
    builder.result
  }
  def dao = { new UserDao }
  def save(user: User) = { dao.save(user) }
}

