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
  private val statusCodes = { code: Int => List(200, 201, 202, 203, 204, 400, 401, 403, 404, 409, 500) contains code }
  def bookmarks(consumer: Consumer, user: User): List[Bookmark] = {
    val http = new Log4jHttp
    val request = bookmarksUrl <@ (consumer, user.accessToken.get)
    val response = http.when(statusCodes)(request ># obj)()
    val bookmarkObjects = ('bookmarks ! (list ! obj))(response)
    val metaObject = ('meta ! obj)(response)
    val conditionsObject = ('conditions ! obj)(response)
    http.shutdown
    bookmarkObjects map BookmarkExtractor
  }
  def bookmarksMeta(consumer: Consumer, user: User): Meta = {
    val http = new Log4jHttp
    val request = bookmarksUrl <<? Map("per_page" -> "1") <@ (consumer, user.accessToken.get)
    val response = http.when(statusCodes)(request ># obj)()
    val metaObject = ('meta ! obj)(response)
    http.shutdown
    MetaExtractor(metaObject)
  }
  def currentUser(consumer: Consumer, user: User): Option[UserData] = {
    val http = new Log4jHttp
    val request = userUrl <@ (consumer, user.accessToken.get)
    val response = http.when(statusCodes)(request ># obj)()
    UserDataExtractor.unapply(response)
  }
}

class Log4jHttp extends nio.Http {
  import org.slf4j.LoggerFactory
  private val logger = LoggerFactory.getLogger(getClass)
  override def make_logger: dispatch.Logger = {
    new dispatch.Logger {
      def info(msg: String, items: Any*): Unit = logger.info(msg.format(items:_*))
    }
  }
}
