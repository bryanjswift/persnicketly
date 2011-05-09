package com.persnicketly.web

import scala.util.matching.Regex
import javax.servlet.http.{Cookie, HttpServlet, HttpServletRequest => Request, HttpServletResponse => Response}

trait Servlet extends HttpServlet {
  override def doGet(request:Request, response:Response) = doGet(new HttpHelper(request, response))
  override def doPost(request:Request, response:Response) = doPost(new HttpHelper(request, response))
  def doGet(http:HttpHelper) { }
  def doPost(http:HttpHelper) { }

  class HttpHelper(val request:Request, val response:Response) {
    // pretty impossible to not match this RE
    private val uriMatch = Servlet.uriRE.findFirstMatchIn(request.getRequestURI).get
    val uri = uriMatch.group("uri")
    val format = uriMatch.group("format") match {
      case null => "html"
      case s:String => s
      case _ => "html"
    }
    val parts = uri.split("/").filter(_.length > 0)
    def apply(param:String, default:String = "") = {
      val value = request.getParameter(param)
      if (value == null || value == "" || value == default) None else Some(value)
    }
    def cookie(key: String, value: String): Unit = {
      val c = new Cookie(key, value)
      c.setDomain(Persnicketly.Config("http.cookie").or(".persnicketly.com"))
      c.setMaxAge(60 * 60 * 24 * 365)
      response.addCookie(c)
    }
    def cookie(key: String): Option[String] = {
      val cookies = request.getCookies
      if (cookies == null) {
        None
      } else {
        cookies.find(c => c.getName == key).map(c => c.getValue)
      }
    }
  }
}

object Servlet {
  private val uriRE = new Regex("(.*?)(\\.(.*))?$", "uri", "junk", "format")
}
