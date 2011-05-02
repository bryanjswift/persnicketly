package com.persnicketly.web

import com.codahale.fig.Configuration
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.ServletContextHandler

object Persnicketly {
  val Config = new Configuration("config.json")
  def main(args:Array[String]) {
    val server = new Server(Config("http.port").as[Int]);
    server.setHandler(new ServletContextHandler())
    // create the context for the webapp
    val webapp = Config("webapp.path").or("src/main/webapp")
    val context = new WebAppContext()
    context.setDescriptor(webapp + "/WEB-INF/web.xml")
    context.setResourceBase(webapp)
    context.setContextPath(Config("webapp.context").or("/"))
    context.setParentLoaderPriority(true)

    // set the webapp context as the handler
    server.setHandler(context)
    // start the server
    server.start()
    server.join()
  }
}
