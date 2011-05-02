package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.web.Servlet
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class RootServlet extends Servlet {
  override def doGet(http: HttpHelper) {
    val view = new VelocityView("/templates/index.vm")
    http.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any](), http.response)
  }
}
