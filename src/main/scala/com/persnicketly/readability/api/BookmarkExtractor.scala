package com.persnicketly.readability.api

import com.codahale.jerkson.AST._
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.{Article, Bookmark}

object BookmarkExtractor {
  def apply(v: JValue): Option[Bookmark] = {
    v match {
      case o: JObject => {
        Some(
          Bookmark(
            None,
            (o \ "id").valueAs[BigInt].toInt,
            (o \ "user_id").valueAs[BigInt].toInt,
            (o \ "favorite").valueAs[Boolean],
            (o \ "archive").valueAs[Boolean],
            ArticleExtractor((o \ "article").asInstanceOf[JObject]).get,
            DateExtractor((o \ "date_archived")),
            DateExtractor((o \ "date_favorited")),
            DateExtractor((o \ "date_updated"))
          )
        )
      }
      case _ => None
    }
  }
}

