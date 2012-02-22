package com.persnicketly.readability

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.persnicketly.Logging
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.model.{Article, Bookmark, Meta, User, UserData}
import com.persnicketly.readability.api._
import com.yammer.metrics.scala.Instrumented
import org.joda.time.DateTime
import org.scribe.model.{Response, OAuthRequest, Verb}

object Api extends Logging with Instrumented {
  private val articlesUrl = "https://www.readability.com/api/rest/v1/articles"
  private val bookmarksUrl = "https://www.readability.com/api/rest/v1/bookmarks"
  private val userUrl = "https://www.readability.com/api/rest/v1/users/_current"

  val datePattern = "YYYY-MM-dd HH:mm:ss"

  lazy val apiMeter = metrics.meter("api-calls", "requests")
  lazy val apiErrorMeter = metrics.meter("api-errors", "errors")

  object Bookmarks {
    def add(user: User, article: Article): Option[String] = { add(user, article.url) }
    def add(user: User, pageUrl: String): Option[String] = {
      val request = new OAuthRequest(Verb.POST, bookmarksUrl)
      request.addBodyParameter("url", pageUrl)

      send(request, user) {
        case ApiResponse(200, response) => response
      }
    }
    def fetch(conditions: BookmarkRequestConditions): Option[List[Bookmark]] = {
      val request = new OAuthRequest(Verb.GET, bookmarksUrl)
      conditions.map.foreach(p => request.addQuerystringParameter(p._1, p._2))

      send(request, conditions.user) {
        case ApiResponse(200, Some(body)) => {
          val marks = (parse[JObject](body) \ "bookmarks") match {
            case JArray(els) => els
            case _ => List[JValue]()
          }
          Some(marks.map(m => BookmarkExtractor(m).get))
        }
      }
    }
    def meta(user: User, since: Option[DateTime] = None): Option[Meta] = {
      val request = new OAuthRequest(Verb.GET, bookmarksUrl)
      request.addQuerystringParameter("per_page", "1")
      since.foreach(s => request.addQuerystringParameter("updated_since", s.toString(datePattern)))

      send(request, user) {
        case ApiResponse(200, Some(body)) => MetaExtractor((parse[JObject](body) \ "meta"))
      }
    }
    def update(user: User, mark: Bookmark): Option[Bookmark] = {
      val request = new OAuthRequest(Verb.POST, bookmarksUrl + "/" + mark.bookmarkId)
      request.addBodyParameter("favorite", (if (mark.isFavorite) "1" else "0"))
      request.addBodyParameter("archive", (if (mark.isArchived) "1" else "0"))

      send(request, user) {
        case ApiResponse(200, Some(body)) => BookmarkExtractor(parse[JObject](body))
      }
    }
  }

  object Articles {
    def apply(user: User, articleId: String): Option[Article] = {
      val request = new OAuthRequest(Verb.GET, articlesUrl + "/" + articleId)

      send(request, user) {
        case ApiResponse(200, Some(body)) => ArticleExtractor(parse[JObject](body))
      }
    }
  }

  def currentUser(user: User): Option[UserData] = {
    val request = new OAuthRequest(Verb.GET, userUrl)
    import scala.actors.Futures.future
    val marks = future { Bookmarks.fetch(BookmarkRequestConditions(1, user)) }

    send(request, user) {
      case ApiResponse(200, Some(body)) =>
        UserDataExtractor(parse[JObject](body)).map(_.copy(userId = marks().flatMap(_.headOption.map(_.userId))))
    }
  }

  type Handler[R] = PartialFunction[ApiResponse[String], R]
  private def send[T](request: OAuthRequest, user: User)(handler: Handler[Option[T]]): Option[T] = {
    apiMeter.mark()
    ReadabilityApi.service.signRequest(user.accessToken.get, request)
    val raw: Response = request.send
    val response = ApiResponse(raw.getCode, raw.getBody)

    log.debug("Request to '{}' responded with {}", request.getUrl, response)

    val defaultHandler: PartialFunction[ApiResponse[String], Option[T]] = {
      case ApiResponse(401, _) => { UserDao.delete(user); None }
      case _ => None
    }

    handler.orElse(defaultHandler)(response)
  }
}

case class ApiResponse[+T](status: Int, body: Option[T])

object ApiResponse {
  def apply(status: Int): ApiResponse[Nothing] = ApiResponse(status, None)
  def apply[T](status: Int, body: T): ApiResponse[T] =
    if (body == null) { ApiResponse(status, None) }
    else { ApiResponse(status, Some(body)) }
}
