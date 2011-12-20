package com.persnicketly.web.servlet

import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging}
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.model.User
import com.persnicketly.web.Servlet
import com.persnicketly.web.servlet.readability.CallbackServlet
import velocity.VelocityView
import javax.ws.rs.core.MediaType

@Singleton
class HomeServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper) {
    helper.uri match {
      case "/" =>
        helper.cookie(Constants.UserCookie) match {
          case Some(id) => UserDao.getById(id) match {
            case Some(user) => CallbackServlet.render(helper, user)
            case None => HomeServlet.render(helper)
          }
          case None => HomeServlet.render(helper)
        }
      case "/thanks" => CallbackServlet.render(helper, User.EMPTY)
      case _ => HomeServlet.render(helper)
    }
  }

}

object HomeServlet extends Logging {

  def render(helper: Servlet#HttpHelper) {
    log.info("Rendering view - /templates/index.vm")
    val view = new VelocityView("/templates/index.vm")
    helper.response.setContentType(MediaType.TEXT_HTML)
    view.render(Map[String,Any]("user" -> helper.cookie(Constants.UserCookie), "uri" -> helper.uri), helper.response)
  }

}
