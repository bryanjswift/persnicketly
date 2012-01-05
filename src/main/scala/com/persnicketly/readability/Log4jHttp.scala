package com.persnicketly.readability

import dispatch.{nio, Request, url}

class Log4jHttp extends nio.Http {
  import org.slf4j.LoggerFactory
  private val logger = LoggerFactory.getLogger(getClass)
  override def make_logger: dispatch.Logger = {
    new dispatch.Logger {
      def info(msg: String, items: Any*) { logger.info(msg.format(items:_*)) }
      def warn(msg: String, items: Any*) { logger.warn(msg.format(items:_*)) }
    }
  }
}

