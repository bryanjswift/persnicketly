package com.persnicketly.readability.model

import org.bson.types.ObjectId
import org.joda.time.DateTime

case class Bookmark(
  id: Option[ObjectId],
  bookmarkId: Int,
  userId: Int,
  isFavorite: Boolean,
  isArchived: Boolean,
  article: Article,
  archivedDate: Option[DateTime],
  favoritedDate: Option[DateTime],
  updatedDate: Option[DateTime])

object Bookmark {
  implicit def asMap(mark: Bookmark) = Map(
                  "favorite" -> (if (mark.isFavorite) "1" else "0"),
                  "archive" -> (if (mark.isArchived) "1" else "0")
                )
}
