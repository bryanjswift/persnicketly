package com.persnicketly.readability.model

import dispatch.json.Js._
import dispatch.json.{Extract, JsValue, JsObject}
import org.joda.time.format.DateTimeFormat

object BookmarkExtractor extends Extract[Bookmark] {
  private val format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")
  def unapply(js: JsValue) = js match {
    case JsObject(m) => 
      if (BookmarkJson.keys.forall(k => m.contains(k))) {
        // can safely extract data
        Some(
          Bookmark(
            BookmarkJson.bookmarkId(js).toInt,
            BookmarkJson.userId(js).toInt,
            BookmarkJson.isFavorite(js),
            BookmarkJson.isArchived(js),
            Article(
              BookmarkJson.ArticleJson.articleId(js),
              BookmarkJson.ArticleJson.title(js),
              BookmarkJson.ArticleJson.url(js),
              BookmarkJson.ArticleJson.excerpt.unapply(js)
            ),
            BookmarkJson.favoritedDate.unapply(js).map(format.parseDateTime),
            BookmarkJson.archivedDate.unapply(js).map(format.parseDateTime),
            BookmarkJson.updatedDate.unapply(js).map(format.parseDateTime)
          )
        )
      } else {
        None
      }
    case _ =>
      None
  }
}

