package com.persnicketly.readability.model

import org.joda.time.DateTime

case class Bookmark(
  user_id: Int,
  read_percent: Double,
  date_updated: DateTime,
  favorite: Boolean,
  id: Int,
  date_archived: Option[DateTime],
  date_opened: Option[DateTime],
  date_added: DateTime,
  article_href: String,
  date_favorited: Option[DateTime],
  archive: Boolean,
  article: Article)

