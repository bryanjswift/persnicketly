package com.persnicketly.readability.model

import com.mongodb.casbah.Imports._
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
  implicit def apply(o: DBObject): Bookmark = {
    Bookmark(
      o._id,
      o.getAsOrElse("bookmark_id", 0),
      o.getAsOrElse("user_id", 0),
      o.getAsOrElse("favorite", false),
      o.getAsOrElse("archive", false),
      Article(o),
      o.getAs[DateTime]("archive_date"),
      o.getAs[DateTime]("favorite_date"),
      o.getAs[DateTime]("update_date")
    )
  }

  implicit def asMap(mark: Bookmark) = Map(
                  "favorite" -> (if (mark.isFavorite) "1" else "0"),
                  "archive" -> (if (mark.isArchived) "1" else "0")
                )

  implicit def bookmark2dbobject(bookmark: Bookmark): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "bookmark_id" -> bookmark.bookmarkId
    builder += "user_id" -> bookmark.userId
    builder += "favorite" -> bookmark.isFavorite
    builder += "archive" -> bookmark.isArchived
    bookmark.archivedDate.foreach(date => builder += "archive_date" -> date)
    bookmark.favoritedDate.foreach(date => builder += "favorite_date" -> date)
    bookmark.updatedDate.foreach(date => builder += "update_date" -> date)
    builder ++= bookmark.article
    builder.result
  }
}
