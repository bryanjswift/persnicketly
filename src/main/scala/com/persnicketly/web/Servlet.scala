package com.persnicketly.web

import com.persnicketly.Persnicketly
import javax.servlet.http.{Cookie, HttpServlet, HttpServletRequest => Request, HttpServletResponse => Response}
import javax.ws.rs.core.MediaType
import scala.util.matching.Regex

trait Servlet extends HttpServlet {
  override def doGet(request:Request, response:Response) = doGet(new HttpHelper(request, response))
  override def doPost(request:Request, response:Response) = doPost(new HttpHelper(request, response))
  def doGet(http:HttpHelper) { }
  def doPost(http:HttpHelper) { }

  class HttpHelper(val request: Request, val response: Response) {
    // pretty impossible to not match this RE
    private val uriMatch = Servlet.uriRE.findFirstMatchIn(request.getRequestURI).get
    val uri = uriMatch.group("uri")
    private var _extras = Map[String, Any]()
    lazy val format = uriMatch.group("format") match {
      case null => "html"
      case s:String => s
      case _ => "html"
    }
    lazy val mime = format match {
      case "atom" => MediaType.APPLICATION_ATOM_XML
      case "rss" => MediaType.APPLICATION_ATOM_XML
      case _ => MediaType.TEXT_HTML
    }
    val parts = uri.split("/").filter(_.length > 0)
    val isAjax = header("X-Requested-With").isDefined
    def addExtra(key: String, value: Any) = {
      _extras += (key -> value)
      this
    }
    def apply(param: String, default: String = "") = {
      val value = request.getParameter(param)
      if (value == null || value == "" || value == default) None else Some(value)
    }
    def cookie(key: String): Option[String] = {
      val cookies = request.getCookies
      if (cookies == null) {
        None
      } else {
        cookies.find(c => c.getName == key).map(c => c.getValue)
      }
    }
    def cookies = new Cookies(request, response)
    def extras = _extras
    def header(name: String): Option[String] = {
      val headerNames = request.getHeaderNames.asInstanceOf[java.util.Enumeration[String]]
      if (headerNames == null) {
        None
      } else {
        headerNames.find(h => h == name).map(h => request.getHeader(h))
      }
    }
    def header(name: String, value: String): Unit = {
      response.setHeader(name, value)
    }
    def write(contentType: String, output: String): Unit = {
      response.setContentType(contentType)
      response.getWriter.write(output)
    }
  }

  class RichEnumeration[T](enumeration: java.util.Enumeration[T]) extends Iterator[T] {
    def hasNext: Boolean = enumeration.hasMoreElements()
    def next: T = enumeration.nextElement()
  }

  implicit def enumerationToRichEnumeration[T](enumeration: java.util.Enumeration[T]): RichEnumeration[T] = {
    new RichEnumeration(enumeration)
  }
}

class Cookies(private val request: Request, private val response: Response) {
  def -(key: String): Cookies = {
    val c = new Cookie(key, "")
    c.setDomain(Persnicketly.Config("http.cookie").or(".persnicketly.com"))
    c.setMaxAge(0)
    c.setPath("/")
    response.addCookie(c)
    this
  }
  def +(key: String, value: String): Cookies = {
    this + (key, value, 60 * 60 * 24 * 365)
  }
  def +(key: String, value: String, age: Int): Cookies = {
    val c = new Cookie(key, value)
    c.setDomain(Persnicketly.Config("http.cookie").or(".persnicketly.com"))
    c.setMaxAge(age)
    c.setPath("/")
    response.addCookie(c)
    this
  }
}

object Servlet {
  private val uriRE = new Regex("(.*?)(\\.(.*))?$", "uri", "junk", "format")
}
