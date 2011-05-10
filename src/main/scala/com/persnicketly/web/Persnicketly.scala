package com.persnicketly.web

import com.codahale.fig.Configuration
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.ServletContextHandler
import dispatch.oauth.Consumer

object Persnicketly {
  val Config = new Configuration("config.json")
  val oauthConsumer = new Consumer("Persnicketly", "ynbCCZ5q7ggBGAkaAGFngRDAChg4pbYm")
  val oauthCallback = String.format("http://%s/readability/callback", Config("http.domain").or("persnicketly.com"))
  def main(args:Array[String]): Unit = {
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
