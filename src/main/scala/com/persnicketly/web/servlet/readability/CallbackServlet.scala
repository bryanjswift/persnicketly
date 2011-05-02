package com.persnicketly.web.servlet.readability

import dispatch.nio.Http
import com.google.inject.Singleton
import com.persnicketly.readability.Auth
import com.persnicketly.web.{Persnicketly, Servlet}
import org.slf4j.LoggerFactory
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class CallbackServlet extends Servlet {
  private val log = LoggerFactory.getLogger(classOf[CallbackServlet])
  override def doGet(helper: HttpHelper) {
    val verifier = helper("oauth_verifier").get
    val token = LoginServlet.tokens.get(helper("oauth_token").get).get
    val http = new Http
    val handler = Auth.access_token(Persnicketly.oauthConsumer, token, verifier);
    val result = http.apply(handler)();
    //val (accessToken, s1, s2) = http.apply(handler)();
    log.info("{}", result)
    val view = new VelocityView("/templates/readability/callback.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    //view.render(Map[String,Any]("access" -> accessToken.toString), helper.response)
    view.render(Map[String,Any](), helper.response)
  }
}
