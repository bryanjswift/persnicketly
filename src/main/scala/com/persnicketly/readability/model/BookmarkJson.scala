package com.persnicketly.readability.model

import dispatch.json.{Js, JsString, Obj}

object BookmarkJson extends Js {
  val bookmarkId = 'id ? num
  val userId = 'user_id ? num
  val isFavorite = 'favorite ? bool
  val isArchived = 'archive ? bool
  object ArticleJson extends Obj('article) {
    val title = 'title ? str
    val url = 'url ? str
    val excerpt = 'excerpt ? str
    val articleId = 'id ? str
    val keys = List(JsString('title), JsString('url), JsString('excerpt), JsString('id))
  }
  val favoritedDate = 'date_favorited ? str
  val archivedDate = 'date_archived ? str
  val updatedDate = 'date_updated ? str
  val keys = List(JsString('id), JsString('user_id), JsString('favorite), JsString('archive), JsString('date_favorited), JsString('date_archived), JsString('date_updated), JsString('article))
}

