package com.persnicketly.web.controller

import com.persnicketly.{Constants, Logging, Persnicketly}
import com.persnicketly.mill.UserQueue
import com.persnicketly.model.ScoredArticle
import com.persnicketly.persistence.{BookmarkDao, ScoredArticleDao, UserDao}
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.Bookmark
import com.persnicketly.web.Servlet
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import velocity.VelocityView
import javax.ws.rs.core.MediaType

object ArticleController extends Logging {
  /**
   * Add article to reader's reading list
   * @param articleId to add
   * @param userId for reading list to append
   */
  def addArticleForUser(articleId: String, userId: Option[String]) {
    UserDao.getById(userId) match {
      case Some(user) =>
        ScoredArticleDao.get(articleId) match {
          // Add article to reading list
          case Some(scored) => {
            Api.Bookmarks.add(Persnicketly.oauthConsumer, user, scored.article)
            UserQueue.add(user)
          }
          // show article not found page
          case None => log.info("No article found with article_id '{}'", articleId)
        }
      case None => log.info("No logged in user with reading list found")
    }
  }

  /**
   * Update the favorite state of a bookmark
   * @param articleId to update
   * @param userId to update article for
   * @param toFavorite this article has been marked a favorite
   * @return Some[Bookmark] if article could be updated
   */
  def updateBookmark(articleId: String, userId: Option[String], toFavorite: Boolean): Option[Bookmark] = {
    UserDao.getById(userId).flatMap(u => {
      ScoredArticleDao.get(articleId).flatMap(s => {
        BookmarkDao.get(u, s.article).flatMap(mark => {
          val result = Api.Bookmarks.update(Persnicketly.oauthConsumer, u, mark.copy(isFavorite = toFavorite))
          // this should trigger a rescore (?)
          result.foreach(BookmarkDao.save)
          result
        })
      })
    })
  }

  /**
   * Render articles using a given template
   * @param helper wrapping HttpServletRequest and HttpServletResponse
   * @param articles to be rendered
   * @param template name of template to use for rendering
   */
  def renderArticles(helper: Servlet#HttpHelper, articles: List[ScoredArticle], template: String) {
    val userId = helper.cookie(Constants.UserCookie)
    val view = new VelocityView(template)
    val now = new DateTime()
    val scored = UserDao.getById(userId) match {
      case Some(u) =>
        articles.map(s => {
          val b = BookmarkDao.get(u, s.article)
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
