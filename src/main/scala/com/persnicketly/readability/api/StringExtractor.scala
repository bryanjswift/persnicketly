package com.persnicketly.readability.api

import com.codahale.jerkson.AST._

object StringExtractor {
  def apply(v: JValue): Option[String] = {
    v match {
      case JString(s) => Some(s)
      case _ => None
    }
  }
}
