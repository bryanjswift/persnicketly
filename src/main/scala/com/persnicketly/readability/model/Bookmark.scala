package com.persnicketly.readability.model

import org.joda.time.format.DateTimeFormat

case class Bookmark(
  bookmarkId: Int,
  userId: Int,
  isFavorite: Boolean,
  isArchived: Boolean,
  article: Article,
  archivedDate: Option[String],
  favoritedDate: Option[String],
  updatedDate: Option[String])

object Bookmark {
  val format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")
}

