package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.web.Persnicketly
import com.persnicketly.readability.model.Bookmark

class BookmarkDao {
  import BookmarkDao._
  RegisterJodaTimeConversionHelpers()
  val config = Persnicketly.Config
  val connection = MongoConnection(config("db.host").or("localhost"), config("db.port").or(27017))
  val bookmarks = connection(config("db.name").or("persnicketly_test"))("bookmarks")
  def save(bookmark: Bookmark) = {
    bookmarks.save(bookmark)
  }
}

object BookmarkDao {
  implicit def bookmark2dbobject(bookmark: Bookmark):DBObject = {
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
    builder += "article_excert" -> bookmark.article.excerpt
    builder.result
  }
}
