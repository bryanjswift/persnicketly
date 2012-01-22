package com.persnicketly.readability.api

import com.codahale.jerkson.AST._
import com.persnicketly.readability.model.UserData

object UserDataExtractor {
  def apply(v: JValue): Option[UserData] = v match {
    case o: JObject =>
      Some(
        UserData(
          (o \ "username").valueAs[String],
          (o \ "first_name").valueAs[String],
          (o \ "last_name").valueAs[String]
        )
      )
    case _ => None
  }
}

