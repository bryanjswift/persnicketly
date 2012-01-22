package com.persnicketly.readability.api

import com.codahale.jerkson.AST._
import com.persnicketly.readability.model.Article

object ArticleExtractor {
  def apply(v: JValue): Option[Article] = {
    v match {
      case o: JObject => {
        Some(
          Article(
            (o \ "id").valueAs[String],
            (o \ "title").valueAs[String],
            (o \ "domain").valueAs[String],
            (o \ "url").valueAs[String],
            StringExtractor((o \ "excerpt")),
            (o \ "processed").valueAs[Boolean]
          )
        )
      }
      case _ => None
    }
  }
}

