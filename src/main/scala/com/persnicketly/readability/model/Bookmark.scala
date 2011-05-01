package com.persnicketly.readability.model

import org.joda.time.DateTime

case class Bookmark(
  user_id: Int,
  read_percent: Double,
  date_updated: String,
  favorite: Boolean,
  id: Int,
  date_archived: Option[String],
  date_opened: Option[String],
  date_added: String,
  date_favorited: Option[String],
  archive: Boolean,
  article: Article)

