package com.persnicketly.readability

import dispatch.{nio, Request, url}
import dispatch.json.JsObject
import dispatch.json.Js._
import dispatch.json.JsHttp.requestToJsHandlers
import dispatch.oauth.Consumer
import dispatch.oauth.OAuth.Request2RequestSigner
import com.persnicketly.Logging
import com.persnicketly.readability.model.{Bookmark, Meta, User, UserData}
import com.persnicketly.readability.api.{BookmarkExtractor, BookmarkRequestConditions, MetaExtractor, UserDataExtractor}
import org.joda.time.DateTime

object Api extends Logging {
  private val bookmarksUrl = url("https://www.readability.com/api/rest/v1/bookmarks")
  private val userUrl = url("https://www.readability.com/api/rest/v1/users/_current")
  private val statusCodes = { code: Int => List(200, 201, 202, 203, 204, 400, 401, 403, 404, 409, 500) contains code }
  private val errorExtractor = 'error ?? bool
  val datePattern = "YYYY-MM-dd HH:mm:ss"
  def bookmarks(consumer: Consumer, conditions: BookmarkRequestConditions): Option[List[Bookmark]] = {
    var url = bookmarksUrl <<? conditions.map
    request(url, consumer, conditions.user) { response =>
      val bookmarkObjects = ('bookmarks ! (list ! obj))(response)
      bookmarkObjects map BookmarkExtractor
    }
  }
  def bookmarksMeta(consumer: Consumer, user: User, since: Option[DateTime] = None): Option[Meta] = {
    var url = bookmarksUrl <<? Map("per_page" -> "1")
    since.foreach(s => url = url <<? Map("updated_since" -> s.toString(datePattern)))
    request(url, consumer, user) { response =>
      val metaObject = ('meta ! obj)(response)
      MetaExtractor(metaObject)
    }
  }
  def currentUser(consumer: Consumer, user: User): Option[UserData] = {
    import scala.actors.Futures.future
    val marks = future { bookmarks(consumer, BookmarkRequestConditions(1, user)) }
    request(userUrl, consumer, user) { response =>
      UserDataExtractor(response).copy(userId = marks().flatMap(_.headOption.map(_.userId)))
    }
  }
  private def request[T](url: Request, consumer: Consumer, user: User)(thunk: JsObject => T): Option[T] = {
    val http = new Log4jHttp
    val request = url <@ (consumer, user.accessToken.get)
    val response = http.when(statusCodes)(request ># obj)()
    val responseStr = if (response == null) { "null" } else { response.toString }
    log.debug("Request to '{}' responded with '{}'", request.path, responseStr)
    try {
      val result = if (isError(response)) { None } else { Some(thunk(response)) }
      result
    } finally {
      http.shutdown
    }
  }
  private def isError(response: JsObject): Boolean = errorExtractor(response).getOrElse(false)
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
