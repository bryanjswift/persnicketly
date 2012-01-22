package com.persnicketly.readability.api

import com.codahale.jerkson.AST._
import com.persnicketly.readability.Api
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

object DateExtractor {
  private val format = DateTimeFormat.forPattern(Api.datePattern)
  def apply(v: JValue): Option[DateTime] = {
    v match {
      case JString(s) => Some(format.parseDateTime(s))
      case _ => None
    }
  }
}
