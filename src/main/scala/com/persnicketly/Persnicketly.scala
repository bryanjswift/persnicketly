package com.persnicketly

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

  private val confProducer = { path: String =>
    log.info("Reading updated config -- {}", path)
    Some(new Configuration(path))
  }
  private val log4jProducer = { path: String =>
    log.info("Reading updated log4j -- {}", path)
    Some(PropertyConfigurator.configure(path))
  }
  private var confResource = new WatchedResource("config.json")(confProducer)
  private var log4jResource = new WatchedResource("log4j.properties")(log4jProducer)
  lazy val oauthConsumer = new Consumer("Persnicketly", "ynbCCZ5q7ggBGAkaAGFngRDAChg4pbYm")
  lazy val oauthCallback = String.format("http://%s/readability/callback", Config("http.domain").or("persnicketly.com"))

  private val opts = new CliOptions
  opts.addOption("v", "velocity", true, "Path to velocity configuration file (velocity.properties)")
  opts.addOption("l", "log4j", true, "Path to log4j configuration file (log4j.properties)")
  opts.addOption("c", "config", true, "Path json configuration file (config.json)")

  private val parser = new GnuParser

  def Config: Configuration = {
    log.info("Reading config files")
    log4jResource.update
    confResource.value.get
  }

  def main(args:Array[String]): Unit = {
    log.info("Persnicketly args -- {}", args)
    val options = parser.parse(opts, args)
    if (options.hasOption("config")) {
      confResource = new WatchedResource(options.getOptionValue("config"))(confProducer)
    }
    if (options.hasOption("velocity")) {
      VelocityHelper.load(options.getOptionValue("velocity"))
    }
    if (options.hasOption("log4j")) {
      log4jResource = new WatchedResource(options.getOptionValue("log4j"))(log4jProducer)
    }
    val server = new Server(Config("http.port").as[Int]);
    server.setHandler(new ServletContextHandler())
    // create the context for the webapp
    val webapp = Config("webapp.path").or("src/main/webapp")
    log.info("Starting server on port {} with path {}", Config("http.port").as[Int], webapp)
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

class WatchedResource[T](val path: String)(producer: String => Option[T]) {
  private val log = LoggerFactory.getLogger(getClass)
  private var lastAccess = 0L
  private var _product: Option[T] = None
  private def file = new File(path)
  private def isUpdated: Boolean = {
    val f = file
    f.exists && f.lastModified > lastAccess
  }
  private def updateWith(p: Option[T]): Unit = {
    val now = (new DateTime).getMillis
    lastAccess = (new DateTime).getMillis
    _product = p
  }
  def value: Option[T] = {
    if (isUpdated) { updateWith(producer(path)) }
    _product
  }
  def update: Unit = value
}

