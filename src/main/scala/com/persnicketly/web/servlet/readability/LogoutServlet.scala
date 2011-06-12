package com.persnicketly.web.servlet.readability

import com.google.inject.Singleton
import com.persnicketly.{Constants, Logging, Persnicketly}
import com.persnicketly.web.Servlet

@Singleton
class LogoutServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper): Unit = {
    helper.cookies - Constants.UserCookie
    helper.response.sendRedirect("/")
  }
}
