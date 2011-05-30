package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.Persnicketly
import com.persnicketly.readability.model.{Article, Bookmark}
import org.bson.types.ObjectId
import org.joda.time.DateTime

class BookmarkDao {
  import BookmarkDao._
  import Persnicketly.Config
  RegisterJodaTimeConversionHelpers()
  private val addresses = Config("db.hosts").or(List(ServerAddress("localhost", 27017)))
  val connection = MongoConnection(addresses.map(_.mongo))
  val bookmarks = connection(Config("db.name").or("persnicketly_test"))("bookmarks")

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
    bookmarks.update(query, bookmark, upsert = true, multi = false)
    bookmarks.findOne(query).get
  }

  /**
   * Before letting this object get collected make sure the connection is closed
   */
  override def finalize() = {
    super.finalize()
    connection.close
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
      o.getAs[Int]("bookmark_id").getOrElse(0),
      o.getAs[Int]("user_id").getOrElse(0),
      o.getAs[Boolean]("favorite").getOrElse(false),
      o.getAs[Boolean]("archive").getOrElse(false),
      Article(
        o.getAs[String]("article_id").getOrElse(""),
        o.getAs[String]("article_title").getOrElse(""),
        o.getAs[String]("article_url").getOrElse(""),
        o.getAs[String]("article_excerpt")
      ),
      o.getAs[DateTime]("archive_date"),
      o.getAs[DateTime]("favorite_date"),
      o.getAs[DateTime]("update_date")
    )
  }
  def dao = { new BookmarkDao }
  def save(bookmark: Bookmark) = { dao.save(bookmark) }
}

