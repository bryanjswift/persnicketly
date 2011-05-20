package com.persnicketly.readability

import dispatch.json.Js._
import dispatch.json.JsHttp.requestToJsHandlers
import dispatch.nio
import dispatch.oauth.Consumer
import dispatch.oauth.OAuth.Request2RequestSigner
import dispatch.url
import com.persnicketly.readability.model.{Bookmark, Meta, User, UserData}
import com.persnicketly.readability.api.{BookmarkExtractor, MetaExtractor, UserDataExtractor}

object Api {
  private val bookmarksUrl = url("https://www.readability.com/api/rest/v1/bookmarks")
  private val userUrl = url("https://www.readability.com/api/rest/v1/users/_current")
  def bookmarks(consumer: Consumer, user: User): List[Bookmark] = {
    val http = new nio.Http
    val request = bookmarksUrl <@ (consumer, user.accessToken.get)
    val response = http(request ># obj)()
    val bookmarkObjects = ('bookmarks ! (list ! obj))(response)
    val metaObject = ('meta ! obj)(response)
    val conditionsObject = ('conditions ! obj)(response)
    http.shutdown
    bookmarkObjects map BookmarkExtractor
  }
  def bookmarksMeta(consumer: Consumer, user: User): Meta = {
    val http = new nio.Http
    val request = bookmarksUrl <<? Map("per_page" -> "1") <@ (consumer, user.accessToken.get)
    val response = http(request ># obj)()
    val metaObject = ('meta ! obj)(response)
    http.shutdown
    MetaExtractor(metaObject)
  }
  def currentUser(consumer: Consumer, user: User): Option[UserData] = {
    val http = new nio.Http
    val request = userUrl <@ (consumer, user.accessToken.get)
    val response = http(request ># obj)()
    UserDataExtractor.unapply(response)
  }
}

