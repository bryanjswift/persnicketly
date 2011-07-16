package com.persnicketly.readability.api

import dispatch.json.Js._
import dispatch.json.{Js, JsString, Obj}
import dispatch.json.{Extract, JsValue, JsObject}
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.{Article, Bookmark}
import org.joda.time.format.DateTimeFormat

object BookmarkExtractor extends Extract[Bookmark] {
  private val format = DateTimeFormat.forPattern(Api.datePattern)
  def unapply(js: JsValue) = js match {
    case JsObject(m) => 
      if (BookmarkJson.keys.forall(k => m.contains(k))) {
        // can safely extract data
        Some(
          Bookmark(
            None,
            BookmarkJson.bookmarkId(js).toInt,
            BookmarkJson.userId(js).toInt,
            BookmarkJson.isFavorite(js),
            BookmarkJson.isArchived(js),
            ArticleExtractor(('article ! obj)(js)),
            BookmarkJson.archivedDate.unapply(js).map(format.parseDateTime),
            BookmarkJson.favoritedDate.unapply(js).map(format.parseDateTime),
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

object BookmarkJson extends Js {
  val bookmarkId = 'id ? num
  val userId = 'user_id ? num
  val isFavorite = 'favorite ? bool
  val isArchived = 'archive ? bool
  val favoritedDate = 'date_favorited ? str
  val archivedDate = 'date_archived ? str
  val updatedDate = 'date_updated ? str
  val keys = List(JsString('id), JsString('user_id), JsString('favorite), JsString('archive), JsString('date_favorited), JsString('date_archived), JsString('date_updated), JsString('article))
}

