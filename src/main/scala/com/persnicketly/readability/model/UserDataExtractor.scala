package com.persnicketly.readability.model

import dispatch.json.Js._
import dispatch.json.{Extract, Js, JsObject, JsString, JsValue}

object UserDataJson extends Js {
  val username = 'username ? str
  val firstName = 'first_name ? str
  val lastName = 'last_name ? str
  val keys = List(JsString('username), JsString('first_name), JsString('last_name))
}

object UserDataExtractor extends Extract[UserData] {
  def unapply(js: JsValue) = js match {
    case JsObject(m) => 
      if (UserDataJson.keys.forall(k => m.contains(k))) {
        // can safely extract data
        Some(UserData(UserDataJson.username(js), UserDataJson.firstName(js), UserDataJson.lastName(js)))
      } else {
        None
      }
    case _ =>
      None
  }
}

