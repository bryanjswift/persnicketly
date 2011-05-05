package com.persnicketly.readability.model

import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

case class Bookmark(
  bookmarkId: Int,
  userId: Int,
  isFavorite: Boolean,
  isArchived: Boolean,
  article: Article,
  archivedDate: Option[DateTime],
  favoritedDate: Option[DateTime],
  updatedDate: Option[DateTime])

object Bookmark {
  val format = DateTimeFormat.forPattern("YYYY-MM-dd HH:mm:ss")
  def apply(archivedDate: Option[String], favoritedDate: Option[String], updatedDate: Option[String],
            bookmarkId: Int, userId: Int, isFavorite: Boolean, isArchived: Boolean, article: Article):Bookmark = {

    Bookmark(
      bookmarkId,
      userId,
      isFavorite,
      isArchived,
      article,
      archivedDate.map(format.parseDateTime),
      favoritedDate.map(format.parseDateTime),
      updatedDate.map(format.parseDateTime))
  }
}

