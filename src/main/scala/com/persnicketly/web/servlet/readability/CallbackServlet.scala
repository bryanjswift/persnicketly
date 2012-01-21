package com.persnicketly.web.servlet.readability

import dispatch.url
import dispatch.oauth.OAuth.Request2RequestSigner
import dispatch.json.Js.obj
import dispatch.json.JsHttp.requestToJsHandlers
import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging, Persnicketly}
import com.persnicketly.mill.UserQueue
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.{Api, Auth}
import com.persnicketly.readability.model.User
import com.persnicketly.web.Servlet
import org.slf4j.LoggerFactory
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class CallbackServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    val verifier = helper("oauth_verifier").get

    // get user based on request token
    val user = UserDao.get(helper("oauth_token").get).get
    val token = user.requestToken
    log.info("auth_token - {} :: verifier - {}", token, verifier)

    // Request an access token
    val accessToken = Auth.accessToken(token, verifier)
    log.info("access_token - {}", accessToken)

    // Get username, first name, last name and userId
    var updatedUser = user.copy(accessToken = Some(accessToken), verifier = Some(verifier))
    val dbUser = UserDao.save(updatedUser.copy(personalInfo = Api.currentUser(Persnicketly.oauthConsumer, updatedUser)))
    log.info("setting _user cookie to {}", dbUser.id.get.toString)
    helper.cookies + (Constants.UserCookie, dbUser.id.get.toString)
    UserQueue.add(dbUser)
    CallbackServlet.render(helper, dbUser)
  }
}

object CallbackServlet extends Logging {
  def render(helper: Servlet#HttpHelper, user: User): Unit = {
    log.info("Rendering view - /templates/callback.vm")
    val view = new VelocityView("/templates/readability/callback.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("personalInfo" -> user.personalInfo, "uri" -> helper.uri), helper.response)
  }
}
