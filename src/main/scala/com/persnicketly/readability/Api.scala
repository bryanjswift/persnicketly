package com.persnicketly.readability

import com.codahale.jerkson.AST._
import com.codahale.jerkson.Json._
import com.persnicketly.Logging
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
    def add(user: User, article: Article) { add(user, article.url) }
    def add(user: User, pageUrl: String): ApiResponse[String] = {
      val request = new OAuthRequest(Verb.POST, bookmarksUrl)
      request.addBodyParameter("url", pageUrl)
      send(request, user) {
        response => { response.getBody }
      }
    }
    def fetch(conditions: BookmarkRequestConditions): Option[List[Bookmark]] = {
      val request = new OAuthRequest(Verb.GET, bookmarksUrl)
      conditions.map.foreach(p => request.addQuerystringParameter(p._1, p._2))
      val apiResponse = send(request, conditions.user) {
        response => {
          val body = response.getBody
          val marks = (parse[JObject](body) \ "bookmarks") match {
            case JArray(els) => els
            case _ => List[JValue]()
          }
          marks.map(m => BookmarkExtractor(m).get)
        }
      }
      apiResponse.body
    }
    def meta(user: User, since: Option[DateTime] = None): Option[Meta] = {
      val request = new OAuthRequest(Verb.GET, bookmarksUrl)
      request.addQuerystringParameter("per_page", "1")
      since.foreach(s => request.addQuerystringParameter("updated_since", s.toString(datePattern)))
      val apiResponse = send(request, user) {
        response => {
          MetaExtractor((parse[JObject](response.getBody) \ "meta")).get
        }
      }
      apiResponse.body
    }
    def update(user: User, mark: Bookmark): Option[Bookmark] = {
      val request = new OAuthRequest(Verb.GET, bookmarksUrl + "/" + mark.bookmarkId)
      request.addBodyParameter("favorite", (if (mark.isFavorite) "1" else "0"))
      request.addBodyParameter("archive", (if (mark.isArchived) "1" else "0"))
      val apiResponse = send(request, user) {
        response => {
          BookmarkExtractor(parse[JObject](response.getBody)).get
        }
      }
      apiResponse.body
    }
  }

  object Articles {
    def apply(user: User, articleId: String): Option[Article] = {
      val request = new OAuthRequest(Verb.GET, articlesUrl + "/" + articleId)
      val apiResponse = send(request, user) {
        response => {
          ArticleExtractor(parse[JObject](response.getBody)).get
        }
      }
      apiResponse.body
    }
  }

  def currentUser(user: User): Option[UserData] = {
    val request = new OAuthRequest(Verb.GET, userUrl)
    import scala.actors.Futures.future
    val marks = future { Bookmarks.fetch(BookmarkRequestConditions(1, user)) }
    val apiResponse = send(request, user) {
      response => {
        UserDataExtractor(parse[JObject](response.getBody)).get.copy(userId = marks().flatMap(_.headOption.map(_.userId)))
      }
    }
    apiResponse.body
  }

  private def send[T](request: OAuthRequest, user: User)(thunk: Response => T): ApiResponse[T] = {
    apiMeter.mark()
    ReadabilityApi.service.signRequest(user.accessToken.get, request)
    val response: Response = request.send

    log.debug("Request to '{}' responded with {}", request.getUrl, response.getBody)

    response.getCode match {
      case 200 => ApiResponse(response.getCode, thunk(response))
      case _ => ApiResponse(response.getCode)
    }
  }
}

case class ApiResponse[+T](status: Int, body: Option[T])

object ApiResponse {
  def apply(status: Int): ApiResponse[Nothing] = ApiResponse(status, None)
  def apply[T](status: Int, body: T): ApiResponse[T] = ApiResponse(status, Some(body))
}
