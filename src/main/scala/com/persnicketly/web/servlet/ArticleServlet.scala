package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.Logging
import com.persnicketly.persistence.ScoredArticleDao
import com.persnicketly.web.Servlet
import com.persnicketly.web.controller.ArticleController
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
      case _ => helper.response.sendRedirect("/")
    }
  }

  def add(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.addArticleForUser(articleId, helper.cookie("_user"))
    list(helper)
  }

  def list(helper: HttpHelper): Unit = {
    val userId = helper.cookie("_user")
    val articles = ScoredArticleDao.find(10)
    val view = new VelocityView("/templates/articleList.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("articles" -> articles, "user" -> userId), helper.response)
  }

  def read(helper: HttpHelper, articleId: String): Unit = {
    ArticleController.addArticleForUser(articleId, helper.cookie("_user"))
    val url = "http://www.readability.com/articles/%s".format(articleId)
    helper.response.sendRedirect(url)
  }
}
