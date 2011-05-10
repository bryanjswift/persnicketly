package com.persnicketly.readability

import dispatch.json.Js._
import dispatch.json.JsHttp.requestToJsHandlers
import dispatch.nio.Http
import dispatch.oauth.Consumer
import dispatch.oauth.OAuth.Request2RequestSigner
import dispatch.url
import com.persnicketly.readability.model.User

object Api {
  private val bookmarksUrl = url("https://www.readability.com/api/rest/v1/bookmarks")
  def bookmarks(consumer: Consumer, user: User) = {
    val http = new Http
    val marks = bookmarksUrl <@ (consumer, user.accessToken.get)
    val response = http(marks ># obj)()
    val bookmarkObjects = ('bookmarks ! (list ! obj))(response)
    val metaObject = ('meta ! obj)(response)
    val conditionsObject = ('conditions ! obj)(response)
  }
}

