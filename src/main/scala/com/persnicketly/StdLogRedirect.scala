package com.persnicketly

import java.io.PrintStream
import org.slf4j.LoggerFactory

object StdLogRedirect {
  def redirectLogs = {
    System.setOut(proxy(System.out, "stdout"))
    System.setErr(proxy(System.err, "stderr"))
  }
  private def proxy(real: PrintStream, name: String): PrintStream = {
    new PrintStream(real) {
      private val log = LoggerFactory.getLogger(name)
      override def print(s: String): Unit = {
        log.info(s)
      }
    }
  }
}

