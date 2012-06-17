package com.persnicketly.web.controller

import com.persnicketly.{Constants, Logging, Persnicketly}
import com.persnicketly.mill.UserQueue
import com.persnicketly.model.{RssArticle, ScoredArticle}
import com.persnicketly.persistence.{ArticleDao, BookmarkDao, Cache, ScoredArticleDao, UserDao}
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.Bookmark
import com.persnicketly.web.Servlet
import org.joda.time.DateTime
import org.scala_tools.time.Imports._
import velocity.VelocityView
import javax.ws.rs.core.MediaType
import com.yammer.metrics.scala.Instrumented

object ArticleController extends Logging with Instrumented {

  lazy val renderTimer = metrics.timer("render")

  /**
   * Add article to reader's reading list
   * @param articleId to add
   * @param userId for reading list to append
   */
  def addArticleForUser(articleId: String, userId: Option[String]) {
    UserDao.getById(userId) match {
      case Some(user) =>
        ArticleDao.get(articleId) match {
          // Add article to reading list
          case Some(article) => {
            Api.Bookmarks.add(user, article)
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
      ArticleDao.get(articleId).flatMap(a => {
        BookmarkDao.get(u, a).flatMap(mark => {
          val result = Api.Bookmarks.update(u, mark.copy(isFavorite = toFavorite))
          // this should trigger a rescore (?)
          UserQueue.add(u)
          result.map(BookmarkDao.save)
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
    renderTimer.time {
      val userId = helper.cookie(Constants.UserCookie)
      val view = new VelocityView(template)
      // This isn't a perfect indicator but it is 'good enough'
      val lastModified = Cache.get[DateTime](Constants.RssUpdated)
      renderHeaders(helper, lastModified)
      val scored = UserDao.getById(userId) match {
        case Some(u) =>
          articles.map(s => {
            val b = BookmarkDao.get(u, s.article)
            s.copy(isBookmarked = b.isDefined, isFavorited = b.map(_.isFavorite).getOrElse(false))
          })
        case None => articles
      }
      view.render(Map[String,Any](
        "articles" -> scored,
        "user" -> userId,
        "uri_base" -> ("http://" + Persnicketly.Config("http.domain").or("persnicketly.com")),
        "uri" -> helper.uri
      ) ++ helper.extras, helper.response)
    }
  }

  /**
   * Render RssArticle instances into an RSS feed
   * @param helper wrapping HttpServletRequest and HttpServletResponse
   * @param articles to be rendered
   */
  def renderRssArticles(helper: Servlet#HttpHelper, articles: List[RssArticle], daysAgo: Int) {
    val view = new VelocityView("/templates/articleList.atom.vm")
    val lastModified = Cache.get[DateTime](Constants.RssUpdated)
    renderHeaders(helper, lastModified)
    view.render(Map[String,Any](
      "articles" -> articles.map(rss => { rss.copy(scored = ScoredArticleDao.get(daysAgo, rss.id.get)) }),
      "uri_base" -> ("http://" + Persnicketly.Config("http.domain").or("persnicketly.com")),
      "uri" -> helper.uri,
      "lastRssUpdate" -> lastModified
    ) ++ helper.extras, helper.response)
  }

  private def renderHeaders(helper: Servlet#HttpHelper, lastModified: Option[DateTime]) {
    val now = new DateTime()
    helper.response.setContentType(helper.mime)
    helper.response.setCharacterEncoding("UTF-8")
    helper.response.setDateHeader("Last-Modified", lastModified.getOrElse(now).getMillis)
    helper.response.setDateHeader("Expires", lastModified.getOrElse(now).plusHours(5).getMillis)
  }
}
