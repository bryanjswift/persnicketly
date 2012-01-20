package com.persnicketly.readability

import dispatch.{Request, url}
import dispatch.json.JsObject
import dispatch.json.Js._
import dispatch.json.JsHttp.requestToJsHandlers
import dispatch.oauth.Consumer
import dispatch.oauth.OAuth.Request2RequestSigner
import com.persnicketly.Logging
import com.persnicketly.readability.model.{Article, Bookmark, Meta, User, UserData}
import com.persnicketly.readability.api._
import org.joda.time.DateTime
import com.yammer.metrics.scala.Instrumented

object Api extends Logging with Instrumented {
  private val articlesUrl = url("https://www.readability.com/api/rest/v1/articles")
  private val bookmarksUrl = url("https://www.readability.com/api/rest/v1/bookmarks")
  private val userUrl = url("https://www.readability.com/api/rest/v1/users/_current")
  private val statusCodes = { code: Int => List(200, 201, 202, 203, 204, 400, 403, 404, 409, 500) contains code }
  private val errorExtractor = 'error ?? bool

  val datePattern = "YYYY-MM-dd HH:mm:ss"

  lazy val apiMeter = metrics.meter("api-calls", "requests")
  lazy val apiErrorMeter = metrics.meter("api-errors", "errors")

  object Bookmarks {
    def add(consumer: Consumer, user: User, article: Article) { add(consumer, user, article.url) }
    def add(consumer: Consumer, user: User, pageUrl: String) {
      val url = bookmarksUrl << Map("url" -> pageUrl)
      // article adding gives an empty response which dispatch translates to null
      request(url, consumer, user) { response => response }
    }
    def fetch(consumer: Consumer, conditions: BookmarkRequestConditions): Option[List[Bookmark]] = {
      var url = bookmarksUrl <<? conditions.map
      request(url, consumer, conditions.user) { response =>
        val bookmarkObjects = ('bookmarks ! (list ! obj))(response)
        bookmarkObjects map BookmarkExtractor
      }
    }
    def meta(consumer: Consumer, user: User, since: Option[DateTime] = None): Option[Meta] = {
      var url = bookmarksUrl <<? Map("per_page" -> "1")
      since.foreach(s => url = url <<? Map("updated_since" -> s.toString(datePattern)))
      request(url, consumer, user) { response =>
        val metaObject = ('meta ! obj)(response)
        MetaExtractor(metaObject)
      }
    }
    def update(consumer: Consumer, user: User, mark: Bookmark): Option[Bookmark] = {
      val url = bookmarksUrl / mark.bookmarkId.toString << mark
      request(url, consumer, user) { response =>
        BookmarkExtractor(response)
      }
    }
  }

  object Articles {
    def apply(consumer: Consumer, user: User, articleId: String): Option[Article] = {
      val url = articlesUrl / articleId
      request(url, consumer, user) { response =>
        ArticleExtractor(response)
      }
    }
  }

  def currentUser(consumer: Consumer, user: User): Option[UserData] = {
    import scala.actors.Futures.future
    val marks = future { Bookmarks.fetch(consumer, BookmarkRequestConditions(1, user)) }
    request(userUrl, consumer, user) { response =>
      UserDataExtractor(response).copy(userId = marks().flatMap(_.headOption.map(_.userId)))
    }
  }

  private def request[T](url: Request, consumer: Consumer, user: User)(thunk: JsObject => T): Option[T] = {
    apiMeter.mark()
    val http = new Log4jHttp
    val request = url <@ (consumer, user.accessToken.get)
    val response = try {
      http.when(statusCodes)(request ># obj)()
    } catch {
      case e: Exception => {
        apiErrorMeter.mark()
        log.error("Error performing request to '{}'", request.path, e)
        null
      }
    }
    val responseStr = if (response == null) { "null || {}" } else { response.toString() }
    log.debug("Request to '{}' responded with '{}'", request.path, responseStr)
    try {
      if (response == null || isError(response)) { None }
      else {
        log.debug("Trying thunk on {}", response)
        Some(thunk(response))
      }
    } catch {
      case e: Exception => {
        log.error("Error performing operation for {}", response)
        None
      }
    } finally {
      http.shutdown()
    }
  }
  private def isError(response: JsObject): Boolean = errorExtractor(response).getOrElse(false)
}
