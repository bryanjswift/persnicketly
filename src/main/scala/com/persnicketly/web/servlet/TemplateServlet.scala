package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.web.Servlet
import velocity.VelocityView
import org.slf4j.LoggerFactory
import javax.ws.rs.core.MediaType

@Singleton
class TemplateServlet extends Servlet {
  private val log = LoggerFactory.getLogger(classOf[TemplateServlet])
  override def doGet(helper: HttpHelper): Unit = {
    log.info("Rendering view - /template{}.vm", helper.uri)
    val view = new VelocityView("/templates%s.vm".format(helper.uri))
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any](), helper.response)
  }
}

