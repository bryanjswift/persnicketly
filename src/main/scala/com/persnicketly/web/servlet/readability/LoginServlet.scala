package com.persnicketly.web.servlet.readability

import dispatch.nio.Http
import com.google.inject.Singleton
import com.persnicketly.readability.Auth
import com.persnicketly.web.{Persnicketly, Servlet}
import org.slf4j.LoggerFactory
import dispatch.oauth.Token
import scala.collection.mutable

@Singleton
class LoginServlet extends Servlet {
  private val log = LoggerFactory.getLogger(classOf[LoginServlet])
  override def doGet(helper: HttpHelper) {
    val handler = Auth.request_token(Persnicketly.oauthConsumer, Persnicketly.oauthCallback)
    val http = new Http
    val token = http.apply(handler)()
    LoginServlet.tokens += (token.value -> token)
    log.info("token = {}", token)
    val authorizeUrl = Auth.authorize_url(token).to_uri.toString
    helper.response.sendRedirect(authorizeUrl)
  }
}

object LoginServlet {
  val tokens = mutable.Map[String, Token]()
}