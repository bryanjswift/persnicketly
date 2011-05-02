package com.persnicketly.web.servlet.readability

import com.google.inject.Singleton
import com.persnicketly.readability.Auth
import com.persnicketly.web.{Persnicketly, Servlet}

@Singleton
class LoginServlet extends Servlet {
  override def doGet(helper: HttpHelper) {
    val handler = Auth.request_token(Persnicketly.oauthConsumer, Persnicketly.oauthCallback)
    val http = new dispatch.nio.Http
    val token = http.apply(handler)()
    val authorizeUrl = Auth.authorize_url(token).to_uri.toString
    helper.response.sendRedirect(authorizeUrl)
  }
}
