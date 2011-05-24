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

