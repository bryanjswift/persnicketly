package com.persnicketly

import org.slf4j.LoggerFactory

trait Logging {
  val loggerName = this.getClass.getName
  @transient lazy val log = LoggerFactory.getLogger(loggerName)
}
