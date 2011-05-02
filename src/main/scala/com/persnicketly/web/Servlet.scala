package com.persnicketly.web

import javax.servlet.http.{HttpServlet, HttpServletRequest => Request, HttpServletResponse => Response}
import scala.util.matching.Regex

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
  }
}

object Servlet {
  private val uriRE = new Regex("(.*?)(\\.(.*))?$", "uri", "junk", "format")
}
