package com.persnicketly.web.servlet.readability

import dispatch.url
import dispatch.nio.Http
import dispatch.oauth.OAuth.Request2RequestSigner
import dispatch.json.Js.obj
import dispatch.json.JsHttp.requestToJsHandlers
import com.google.inject.Singleton
import com.persnicketly.readability.{Api, Auth}
import com.persnicketly.web.{Persnicketly, Servlet}
import com.persnicketly.persistence.UserDao
import org.slf4j.LoggerFactory
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class CallbackServlet extends Servlet {
  private val log = LoggerFactory.getLogger(classOf[CallbackServlet])

  override def doGet(helper: HttpHelper) {
    val verifier = helper("oauth_verifier").get
    val user = UserDao.get(helper("oauth_token").get)
    val token = user.get.requestToken
    log.info("auth_token - {} :: verifier - {}", token, verifier)
    val http = new Http
    val accessToken = http(Auth.access_token(Persnicketly.oauthConsumer, token, verifier))();
    val consumer = Persnicketly.oauthConsumer
    log.info("access_token - {}", accessToken)
    var updatedUser = user.get.copy(accessToken = Some(accessToken), verifier = Some(verifier))
    val userInfo = Api.currentUser(consumer, updatedUser)
    updatedUser = updatedUser.copy(personalInfo = userInfo)
    val dbUser = UserDao.save(updatedUser)
    log.info("setting _user cookie to {}", dbUser.id.get.toString)
    helper.cookie("_user", dbUser.id.get.toString)
    val view = new VelocityView("/templates/readability/callback.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("personalInfo" -> dbUser.personalInfo), helper.response)
  }
}
