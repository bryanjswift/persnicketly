package com.persnicketly.web.servlet.readability

import com.google.inject.Singleton
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.Auth
import com.persnicketly.readability.model.User
import com.persnicketly.web.{Persnicketly, Servlet}
import org.slf4j.LoggerFactory
import dispatch.oauth.Token
import scala.collection.mutable

@Singleton
class LoginServlet extends Servlet {
  private val log = LoggerFactory.getLogger(classOf[LoginServlet])
  override def doGet(helper: HttpHelper) {
    val token = Auth.request_token(Persnicketly.oauthConsumer, Persnicketly.oauthCallback)
    // save user
    val user = UserDao.save(User(None, token, None, None, None))
    // set cookie to ObjectId of User
    log.info("auth_token = {}", token)
    val authorizeUrl = Auth.authorize_url(token).to_uri.toString
    helper.response.sendRedirect(authorizeUrl)
  }
}

