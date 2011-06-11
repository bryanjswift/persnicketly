package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.{Logging, Persnicketly}
import com.persnicketly.web.Servlet
import com.persnicketly.persistence.ScoredArticleDao
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class ArticleServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    val userId = helper.cookie("_user")
    val articles = ScoredArticleDao.find(10)
    val view = new VelocityView("/templates/articleList.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("articles" -> articles, "user" -> userId), helper.response)
  }
}
