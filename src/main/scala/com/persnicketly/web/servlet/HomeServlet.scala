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
    log.info("Rendering view - /templates/index.vm")
    val view = new VelocityView("/templates/index.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("user" -> helper.cookie(Constants.UserCookie)), helper.response)
  }
}

