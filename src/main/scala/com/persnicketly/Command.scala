package com.persnicketly

import org.apache.commons.cli.{CommandLine, GnuParser}

trait Command extends Logging {
  type CliOpts = CommandLine

  def group: ThreadGroup
  def start(options: CliOpts): List[Thread]

  def start(args: Array[String]): List[Thread] = {
    // Parse args
    val parser = new GnuParser
    val options = parser.parse(Persnicketly.cliOpts, args)
    start(options)
  }

  def async(name: String, work: => Unit): Thread = {
    log.info("Starting thread '{}'", name)
    val t = new Thread(group, name) { override def run() = work }
    t.start
    t
  }
}
