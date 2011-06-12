package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging}
import com.persnicketly.web.Servlet
import velocity.VelocityView
import org.slf4j.LoggerFactory
import javax.ws.rs.core.MediaType

@Singleton
class HomeServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    val userId = helper.cookie(Constants.UserCookie)
    userId match {
      case Some(_) => helper.response.sendRedirect(ArticleServlet.listUrl)
      case None => {
        log.info("Rendering view - /templates/index.vm")
        val view = new VelocityView("/templates/index.vm")
        helper.response.setContentType(MediaType.TEXT_HTML)
        view.render(Map[String,Any]("user" -> userId), helper.response)
      }
    }
  }
}

