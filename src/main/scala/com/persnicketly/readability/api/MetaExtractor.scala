package com.persnicketly.readability.api

import com.codahale.jerkson.AST._
import com.persnicketly.readability.model.Meta

object MetaExtractor {
  def apply(v: JValue): Option[Meta] = v match {
    case o: JObject =>
      Some(
        Meta(
          (o \ "num_pages").valueAs[BigInt].toInt,
          (o \ "page").valueAs[BigInt].toInt,
          (o \ "item_count_total").valueAs[BigInt].toInt,
          (o \ "item_count").valueAs[BigInt].toInt
        )
      )
    case _ => None
  }
}

