package com.persnicketly.readability.model

import org.bson.types.ObjectId
import org.joda.time.DateTime
import dispatch.oauth.Token

case class User(
  id: Option[ObjectId],
  requestToken: Token,
  accessToken: Option[Token],
  verifier: Option[String],
  lastProcessed: Option[DateTime],
  personalInfo: Option[UserData])

object TokenHelper {
  def apply(value: Option[String], secret: Option[String]) = {
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
