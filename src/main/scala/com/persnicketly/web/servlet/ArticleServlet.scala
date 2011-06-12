package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging}
import com.persnicketly.persistence.ScoredArticleDao
import com.persnicketly.web.Servlet
import com.persnicketly.web.controller.ArticleController
import org.apache.http.HttpStatus
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class ArticleServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    log.info("Parts for uri :: '{}' for '{}'", helper.parts, helper.uri)
    helper.parts match {
      case Array("article", "list") => list(helper)
      case Array("article", "read", articleId) => read(helper, articleId)
      case Array("article", "add", articleId) => add(helper, articleId)
      case _ => helper.response.sendError(HttpStatus.SC_NOT_FOUND)
    }
  }

  def add(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.addArticleForUser(articleId, helper.cookie(Constants.UserCookie))
    // it would be nice to say an article wasn't found or you're not logged in
    helper.response.sendRedirect(ArticleServlet.listUrl)
  }

  def list(helper: HttpHelper): Unit = {
    val userId = helper.cookie(Constants.UserCookie)
    val articles = ScoredArticleDao.find(10)
    val view = new VelocityView("/templates/articleList.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("articles" -> articles, "user" -> userId), helper.response)
  }

  def read(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.addArticleForUser(articleId, helper.cookie(Constants.UserCookie))
    // it would be nice to say an article wasn't found or you're not logged in
    val url = "http://www.readability.com/articles/%s".format(articleId)
    helper.response.sendRedirect(url)
  }
}

object ArticleServlet {
  val listParts = Array("article", "list")
  val listUrl = listParts.mkString("/", "/", "")
}
