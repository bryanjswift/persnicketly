package com.persnicketly.web.servlet

import com.codahale.jerkson.Json._
import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging}
import com.persnicketly.persistence.ScoredArticleDao
import com.persnicketly.web.{JsonResponse, Servlet}
import com.persnicketly.web.controller.ArticleController
import org.apache.http.HttpStatus
import javax.ws.rs.core.MediaType

@Singleton
class ArticleServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    log.info("Parts for uri :: '{}' for '{}'", helper.parts, helper.uri)
    helper.parts match {
      case Array("article", "add", articleId) => add(helper, articleId)
      case Array("article", "list") => list(helper)
      case Array("article", "read", articleId) => read(helper, articleId)
      case Array("article", "recent") => recent(helper)
      case Array("article", "star", articleId) => star(helper, articleId)
      case Array("article", "unstar", articleId) => unstar(helper, articleId)
      case _ => helper.response.sendError(HttpStatus.SC_NOT_FOUND)
    }
  }

  def add(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.addArticleForUser(articleId, helper.cookie(Constants.UserCookie))
    // it would be nice to say an article wasn't found or you're not logged in
    if (helper.isAjax) {
      helper.write(Constants.ApplicationJson, generate(JsonResponse(200)))
    } else {
      helper.response.sendRedirect(ArticleServlet.listUrl)
    }
  }

  def list(helper: HttpHelper) =
    ArticleController.renderArticles(helper, ScoredArticleDao.find(10), "/templates/articleList.vm")

  def read(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.addArticleForUser(articleId, helper.cookie(Constants.UserCookie))
    // it would be nice to say an article wasn't found or you're not logged in
    val url = "http://www.readability.com/articles/%s".format(articleId)
    helper.response.sendRedirect(url)
  }

  def recent(helper: HttpHelper) =
    ArticleController.renderArticles(helper, ScoredArticleDao.recent(10), "/templates/articleRecent.vm")

  def star(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.updateBookmark(articleId, helper.cookie(Constants.UserCookie), toFavorite = true)
    // it would be nice to say an article wasn't found or you're not logged in
    if (helper.isAjax) {
      helper.write(Constants.ApplicationJson, generate(JsonResponse(200)))
    } else {
      helper.response.sendRedirect(ArticleServlet.listUrl)
    }
  }

  def unstar(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.updateBookmark(articleId, helper.cookie(Constants.UserCookie), toFavorite = false)
    // it would be nice to say an article wasn't found or you're not logged in
    if (helper.isAjax) {
      helper.write(Constants.ApplicationJson, generate(JsonResponse(200)))
    } else {
      helper.response.sendRedirect(ArticleServlet.listUrl)
    }
  }

}

object ArticleServlet {
  val listParts = Array("article", "list")
  val listUrl = listParts.mkString("/", "/", "")
}
