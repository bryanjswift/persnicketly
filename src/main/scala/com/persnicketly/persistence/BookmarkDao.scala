package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.Persnicketly
import com.persnicketly.readability.model.{Article, Bookmark}
import org.bson.types.ObjectId
import org.joda.time.DateTime

class BookmarkDao extends Dao {
  import BookmarkDao._
  val collectionName = "bookmarks"

  // Initialize indexes
  collection.ensureIndex(MongoDBObject("article_id" -> 1, "user_id" -> 1))
  collection.ensureIndex(MongoDBObject("bookmark_id" -> 1))

  /**
   * Save bookmark data by updating existing record or inserting new
   * @param bookmark data to save
   * @return Bookmark as it now exists in database
   */
  def save(bookmark: Bookmark): Bookmark = {
    val query = bookmark.id match {
      case Some(id) => MongoDBObject("_id" -> id)
      case None => MongoDBObject("bookmark_id" -> bookmark.bookmarkId)
    }
    collection.update(query, bookmark, upsert = true, multi = false)
    collection.findOne(query).get
  }
}

object BookmarkDao {
  implicit def bookmark2dbobject(bookmark: Bookmark): DBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "bookmark_id" -> bookmark.bookmarkId
    builder += "user_id" -> bookmark.userId
    builder += "favorite" -> bookmark.isFavorite
    builder += "archive" -> bookmark.isArchived
    if (bookmark.archivedDate.isDefined) { builder += "archive_date" -> bookmark.archivedDate.get }
    if (bookmark.favoritedDate.isDefined) { builder += "favorite_date" -> bookmark.favoritedDate.get }
    if (bookmark.updatedDate.isDefined) { builder += "update_date" -> bookmark.updatedDate.get }
    builder += "article_id" -> bookmark.article.articleId
    builder += "article_title" -> bookmark.article.title
    builder += "article_url" -> bookmark.article.url
    builder += "article_excerpt" -> bookmark.article.excerpt
    builder.result
  }

  implicit def dbobject2bookmark(o: DBObject): Bookmark = {
    Bookmark(
      o._id,
      o.getAsOrElse("bookmark_id", 0),
      o.getAsOrElse("user_id", 0),
      o.getAsOrElse("favorite", false),
      o.getAsOrElse("archive", false),
      Article(
        o.getAsOrElse("article_id", ""),
        o.getAsOrElse("article_title", ""),
        o.getAsOrElse("article_url", ""),
        o.getAs[String]("article_excerpt")
      ),
      o.getAs[DateTime]("archive_date"),
      o.getAs[DateTime]("favorite_date"),
      o.getAs[DateTime]("update_date")
    )
  }

  private def dao = { new BookmarkDao }

  def save(bookmark: Bookmark) = { dao.save(bookmark) }
}

