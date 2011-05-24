package com.persnicketly.readability

import dispatch.{nio, Request, url}
import dispatch.json.JsObject
import dispatch.json.Js._
import dispatch.json.JsHttp.requestToJsHandlers
import dispatch.oauth.Consumer
import dispatch.oauth.OAuth.Request2RequestSigner
import com.persnicketly.readability.model.{Bookmark, Meta, User, UserData}
import com.persnicketly.readability.api.{BookmarkExtractor, MetaExtractor, UserDataExtractor}

object Api {
  private val bookmarksUrl = url("https://www.readability.com/api/rest/v1/bookmarks")
  private val userUrl = url("https://www.readability.com/api/rest/v1/users/_current")
  private val statusCodes = { code: Int => List(200, 201, 202, 203, 204, 400, 401, 403, 404, 409, 500) contains code }
  val datePattern = "YYYY-MM-dd HH:mm:ss"
  def bookmarks(consumer: Consumer, user: User): List[Bookmark] = {
    request(bookmarksUrl, consumer, user) { response =>
      val bookmarkObjects = ('bookmarks ! (list ! obj))(response)
      bookmarkObjects map BookmarkExtractor
    }
  }
  def bookmarksMeta(consumer: Consumer, user: User): Meta = {
    val url = bookmarksUrl <<? Map("per_page" -> "1")
    request(url, consumer, user) { response =>
      val metaObject = ('meta ! obj)(response)
      MetaExtractor(metaObject)
    }
  }
  def currentUser(consumer: Consumer, user: User): Option[UserData] = {
    request(userUrl, consumer, user) { response =>
      UserDataExtractor.unapply(response)
    }
  }
  private def request[T](url: Request, consumer: Consumer, user: User)(thunk: JsObject => T) = {
    val http = new Log4jHttp
    val request = url <@ (consumer, user.accessToken.get)
    val response = http.when(statusCodes)(request ># obj)()
    val result = thunk(response) // if this throws an exception we never shutdown http
    http.shutdown
    result
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
