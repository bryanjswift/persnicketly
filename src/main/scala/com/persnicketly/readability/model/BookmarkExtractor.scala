package com.persnicketly.readability.model

import dispatch.json.Js._
import dispatch.json.{Extract, JsValue, JsObject}

object BookmarkExtractor extends Extract[Bookmark] {
  def unapply(js: JsValue) = js match {
    case JsObject(m) => 
      if (BookmarkJson.keys.forall(k => m.contains(k))) {
        // can safely extract data
        Some(
          Bookmark(
            BookmarkJson.favoritedDate.unapply(js),
            BookmarkJson.archivedDate.unapply(js),
            BookmarkJson.updatedDate.unapply(js),
            BookmarkJson.bookmarkId(js).toInt,
            BookmarkJson.userId(js).toInt,
            BookmarkJson.isFavorite(js),
            BookmarkJson.isArchived(js),
            Article(
              BookmarkJson.ArticleJson.title(js),
              BookmarkJson.ArticleJson.url(js),
              BookmarkJson.ArticleJson.articleId(js),
              BookmarkJson.ArticleJson.excerpt.unapply(js)
            )
          )
        )
      } else {
        None
      }
    case _ =>
      None
  }
}

