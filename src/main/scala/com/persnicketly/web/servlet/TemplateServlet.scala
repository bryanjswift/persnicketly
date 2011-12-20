package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging}
import com.persnicketly.web.Servlet
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class TemplateServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    log.info("Rendering view - /templates{}.vm", helper.uri)
    val view = new VelocityView("/templates%s.vm".format(helper.uri))
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("user" -> helper.cookie(Constants.UserCookie)), helper.response)
  }
}

