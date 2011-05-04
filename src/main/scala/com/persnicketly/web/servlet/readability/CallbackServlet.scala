package com.persnicketly.web.servlet.readability

import dispatch.url
import dispatch.nio.Http
import dispatch.oauth.OAuth.Request2RequestSigner
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
    log.info("auth_token - {} :: verifier - {}", token, verifier)
    val http = new Http
    val accessToken = http(Auth.access_token(Persnicketly.oauthConsumer, token, verifier))();
    log.info("access_token - {}", accessToken)
    val marks = url("https://www.readability.com/api/rest/v1/bookmarks") <@ (Persnicketly.oauthConsumer, accessToken)
    val result = http(marks as_str)()
    val view = new VelocityView("/templates/readability/callback.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("data" -> result), helper.response)
  }
}
