package com.persnicketly

import java.io.File
import com.codahale.fig.Configuration
import com.persnicketly.mill.Foreman
import com.persnicketly.web.Spider
import org.apache.commons.cli.{Options => CliOptions, GnuParser}
import org.apache.log4j.PropertyConfigurator
import org.joda.time.DateTime
import org.slf4j.LoggerFactory
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

  val cliOpts = new CliOptions
  cliOpts.addOption("v", "velocity", true, "Path to velocity configuration file (velocity.properties)")
  cliOpts.addOption("l", "log4j", true, "Path to log4j configuration file (log4j.properties)")
  cliOpts.addOption("c", "config", true, "Path json configuration file (config.json)")
  cliOpts.addOption("m", "mill", false, "Start the queue processing mill")
  cliOpts.addOption("w", "web", false, "Start the web server")
  cliOpts.addOption("s", "scheduled", false, "Schedule periodic mill updates")

  def Config: Configuration = {
    log4jResource.update
    confResource.value.get
  }

  def main(args: Array[String]): Unit = {
    log.info("Persnicketly args -- {}", args)
    val parser = new GnuParser
    val options = parser.parse(cliOpts, args)
    if (options.hasOption("config")) {
      confResource = new WatchedResource(options.getOptionValue("config"))(confProducer)
    }
    if (options.hasOption("velocity")) {
      VelocityHelper.load(options.getOptionValue("velocity"))
    }
    if (options.hasOption("log4j")) {
      log4jResource = new WatchedResource(options.getOptionValue("log4j"))(log4jProducer)
    }
    var threads: List[Thread] = Nil
    // start the mill
    if (options.hasOption("mill") || options.hasOption("scheduled")) {
      Foreman.start(args) ::: threads
    }
    // start the web
    if (options.hasOption("web")) {
      Spider.start(args) ::: threads
    }
    // join all threads
    threads.foreach(_.join)
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

