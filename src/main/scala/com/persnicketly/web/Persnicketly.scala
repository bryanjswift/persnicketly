package com.persnicketly.web

import java.io.File
import com.codahale.fig.Configuration
import org.apache.commons.cli.{Options => CliOptions, GnuParser}
import org.apache.log4j.PropertyConfigurator
import org.eclipse.jetty.server.Server
import org.eclipse.jetty.webapp.WebAppContext
import org.eclipse.jetty.servlet.ServletContextHandler
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
import dispatch.oauth.Consumer
import velocity.VelocityHelper

object Persnicketly {
  private val log = LoggerFactory.getLogger(getClass)

  private var confResource = new WatchedResource[Configuration]("config.json")
  private var log4jResource: Option[WatchedResource[_]] = None
  val oauthConsumer = new Consumer("Persnicketly", "ynbCCZ5q7ggBGAkaAGFngRDAChg4pbYm")
  val oauthCallback = String.format("http://%s/readability/callback", Config("http.domain").or("persnicketly.com"))

  private val opts = new CliOptions
  opts.addOption("v", "velocity", true, "Path to velocity configuration file (velocity.properties)")
  opts.addOption("l", "log4j", true, "Path to log4j configuration file (log4j.properties)")
  opts.addOption("c", "config", true, "Path json configuration file (config.json)")

  private val parser = new GnuParser

  def Config = {
    if (confResource.isUpdated) {
      confResource.product = Some(new Configuration(confResource.path))
    }
    if (log4jResource.isDefined && log4jResource.get.isUpdated) {
      PropertyConfigurator.configure(log4jResource.get.path)
    }
    confResource.product.get
  }

  def main(args:Array[String]): Unit = {
    log.info("Persnicketly args -- {}", args)
    val options = parser.parse(opts, args)
    if (options.hasOption("config")) {
      confResource = new WatchedResource[Configuration](options.getOptionValue("config"))
    }
    if (options.hasOption("velocity")) {
      VelocityHelper.load(options.getOptionValue("velocity"))
    }
    if (options.hasOption("log4j")) {
      log4jResource = Some(new WatchedResource[Nothing](options.getOptionValue("log4j")))
    }
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

class WatchedResource[T](val path: String) {
  private var lastAccess = 0L
  var product: Option[T] = None
  val file = new File(path)
  def exists: Boolean = file.exists
  def isUpdated: Boolean = file.lastModified > lastAccess
}

