package com.persnicketly.readability.model

import com.mongodb.casbah.Imports._
import org.bson.types.ObjectId
import org.joda.time.DateTime

case class User(
  id: Option[ObjectId],
  requestToken: Token,
  accessToken: Option[Token],
  verifier: Option[String],
  lastProcessed: Option[DateTime],
  personalInfo: Option[UserData])

object User {
  val EMPTY = User(None, null, None, None, None, None)

  implicit def apply(o: DBObject): User = {
    val rtv = o.getAs[String]("request_token_value")
    val rts = o.getAs[String]("request_token_secret")
    val atv = o.getAs[String]("access_token_value")
    val ats = o.getAs[String]("access_token_secret")
    User(
      o._id,
      TokenHelper(rtv, rts).get,
      TokenHelper(atv, ats),
      o.getAs[String]("verifier"),
      o.getAs[DateTime]("last_processed"),
      UserData(
        o.getAs[Int]("user_id"),
        o.getAs[String]("username"),
        o.getAs[String]("first_name"),
        o.getAs[String]("last_name")
      )
    )
  }

  implicit def user2dbobject(user: User): DBObject = {
    val builder = MongoDBObject.newBuilder
    user.id.foreach(id => builder += ("_id" -> id))
    builder += "request_token_value" -> user.requestToken.value
    builder += "request_token_secret" -> user.requestToken.secret
    user.accessToken.foreach(t => {
      builder += "access_token_value" -> t.value
      builder += "access_token_secret" -> t.secret
    })
    user.verifier.foreach(v => builder += ("verifier" -> v))
    user.personalInfo.foreach(info => {
      builder += "user_id" -> info.userId
      builder += "username" -> info.username
      builder += "first_name" -> info.firstName
      builder += "last_name" -> info.lastName
    })
    user.lastProcessed.foreach(d => builder += ("last_processed" -> d))
    builder += ("last_updated" -> new DateTime)
    builder.result
  }
}

object TokenHelper {
  def apply(value: Option[String], secret: Option[String]): Option[Token] = {
    if (value.isDefined && secret.isDefined) {
      Some(Token(value.get, secret.get))
    } else {
      None
    }
  }
}

case class UserData(userId: Option[Int], username: String, firstName: String, lastName: String)

object UserData {
  val Empty = new UserData(None, "", "", "")
  def apply(username: String, firstName: String, lastName: String): UserData = {
    UserData(None, username, firstName, lastName)
  }
  def apply(userId: Option[Int], username: Option[String], firstName:Option[String], lastName: Option[String]): Option[UserData] = {
    if (username.isDefined && firstName.isDefined && lastName.isDefined) {
      Some(UserData(userId, username.get, firstName.get, lastName.get))
    } else {
      None
    }
  }
  def apply(username: Option[String], firstName:Option[String], lastName: Option[String]): Option[UserData] = {
    if (username.isDefined && firstName.isDefined && lastName.isDefined) {
      Some(UserData(None, username.get, firstName.get, lastName.get))
    } else {
      None
    }
  }
}
