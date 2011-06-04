package com.persnicketly.web

import com.persnicketly.{Command,Persnicketly}
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.ServletContextHandler

object Spider extends Command {
  val group = new ThreadGroup("Web")

  def start(options: CliOpts): List[Thread] = {
    List(async("jetty", startServer))
  }

  def startServer(): Unit = {
    val config = Persnicketly.Config
    // Setup the server
    val server = new Server(config("http.port").as[Int]);
    server.setHandler(new ServletContextHandler())
    // create the context for the webapp
    val webapp = config("webapp.path").or("src/main/webapp")
    log.info("Starting server on port {} with path {}", config("http.port").as[Int], webapp)
    val context = new WebAppContext()
    context.setDescriptor(webapp + "/WEB-INF/web.xml")
    context.setResourceBase(webapp)
    context.setContextPath(config("webapp.context").or("/"))
    context.setParentLoaderPriority(true)

    // set the webapp context as the handler
    server.setHandler(context)
    // start the server
    server.start()
    server.join()
  }
}
