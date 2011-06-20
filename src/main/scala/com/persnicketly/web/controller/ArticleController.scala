package com.persnicketly.web.controller

import com.persnicketly.{Constants, Logging, Persnicketly}
import com.persnicketly.mill.UserQueue
import com.persnicketly.model.ScoredArticle
import com.persnicketly.persistence.{BookmarkDao, ScoredArticleDao, UserDao}
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.User
import com.persnicketly.web.Servlet
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import velocity.VelocityView
import javax.ws.rs.core.MediaType

object ArticleController extends Logging {
  def addArticleForUser(articleId: String, userId: Option[String]): Unit = {
    UserDao.getById(userId.getOrElse("")) match {
      case Some(user) =>
        ScoredArticleDao.get(articleId) match {
          // Add article to reading list
          case Some(scored) => {
            Api.bookmark(Persnicketly.oauthConsumer, user, scored.article)
            UserQueue.add(user)
          }
          // show article not found page
          case None => log.info("No article found with article_id '{}'", articleId)
        }
      case None => log.info("No logged in user, just redirecting")
    }
  }

  def renderArticles(helper: Servlet#HttpHelper, articles: List[ScoredArticle], template: String): Unit = {
    val userId = helper.cookie(Constants.UserCookie)
    val view = new VelocityView(template)
    val now = new DateTime()
    val scored = UserDao.getById(userId) match {
      case Some(u) =>
        articles.map(s => {
          val b = BookmarkDao.hasBookmark(u, s.article)
          s.copy(isBookmarked = b.isDefined, isFavorited = b.map(_.isFavorite).getOrElse(false))
        })
      case None => articles
    }
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any](
      "articles" -> scored,
      "user" -> userId,
      "uri" -> helper.uri,
      "today" -> now,
      "yesterday" -> (now - 1.day),
      "weekAgo" -> (now - 8.days)
    ), helper.response)
  }
}
