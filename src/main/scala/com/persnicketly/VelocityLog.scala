package com.persnicketly

import org.apache.velocity.runtime.{RuntimeServices => Services}
import org.apache.velocity.runtime.log.LogChute
import org.slf4j.LoggerFactory

class VelocityLog extends LogChute {
  private val logger = LoggerFactory.getLogger(this.getClass);
  def init(services:Services) = { }
  def isLevelEnabled(level:Int) = {
    level match {
      case LogChute.ERROR_ID => logger.isErrorEnabled
      case LogChute.WARN_ID => logger.isWarnEnabled
      case LogChute.INFO_ID => logger.isInfoEnabled
      case LogChute.DEBUG_ID => logger.isDebugEnabled
      case LogChute.TRACE_ID => logger.isTraceEnabled
      case _ => logger.isTraceEnabled
    }
  }
  def log(level:Int, message:String) = {
    level match {
      case LogChute.ERROR_ID => logger.error(message)
      case LogChute.WARN_ID => logger.warn(message)
      case LogChute.INFO_ID => logger.info(message)
      case LogChute.DEBUG_ID => logger.debug(message)
      case LogChute.TRACE_ID => logger.trace(message)
      case _ => logger.trace(message)
    }
  }
  def log(level:Int, message:String, t:Throwable) = {
    level match {
      case LogChute.ERROR_ID => logger.error(message, t)
      case LogChute.WARN_ID => logger.warn(message, t)
      case LogChute.INFO_ID => logger.info(message, t)
      case LogChute.DEBUG_ID => logger.debug(message, t)
      case LogChute.TRACE_ID => logger.trace(message, t)
      case _ => logger.trace(message, t)
    }
  }
}
