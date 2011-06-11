package com.persnicketly.web.servlet.readability

import com.google.inject.Singleton
import com.persnicketly.{Logging, Persnicketly}
import com.persnicketly.web.Servlet

@Singleton
class LogoutServlet extends Servlet with Logging {

  override def doGet(helper: HttpHelper): Unit = {
    helper.cookies - "_user"
    helper.response.sendRedirect("/")
  }
}
