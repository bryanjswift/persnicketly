package com.persnicketly.web.servlet.readability

import dispatch.url
import dispatch.oauth.OAuth.Request2RequestSigner
import dispatch.json.Js.obj
import dispatch.json.JsHttp.requestToJsHandlers
import com.google.inject.Singleton
import com.persnicketly.readability.{Api, Auth}
import com.persnicketly.Persnicketly
import com.persnicketly.web.Servlet
import com.persnicketly.persistence.UserDao
import com.persnicketly.mill.UserQueue
import org.slf4j.LoggerFactory
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class CallbackServlet extends Servlet {
  private val log = LoggerFactory.getLogger(classOf[CallbackServlet])

  override def doGet(helper: HttpHelper) {
    val verifier = helper("oauth_verifier").get

    // get user based on request token
    val user = UserDao.get(helper("oauth_token").get).get
    val token = user.requestToken
    log.info("auth_token - {} :: verifier - {}", token, verifier)

    // Request an access token
    val accessToken = Auth.access_token(Persnicketly.oauthConsumer, token, verifier)
    log.info("access_token - {}", accessToken)

    // Get username, first name, last name and userId
    var updatedUser = user.copy(accessToken = Some(accessToken), verifier = Some(verifier))
    val dbUser = UserDao.save(updatedUser.copy(personalInfo = Api.currentUser(Persnicketly.oauthConsumer, updatedUser)))
    log.info("setting _user cookie to {}", dbUser.id.get.toString)
    helper.cookie("_user", dbUser.id.get.toString)
    UserQueue.add(dbUser)
    val view = new VelocityView("/templates/readability/callback.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("personalInfo" -> dbUser.personalInfo), helper.response)
  }
}
