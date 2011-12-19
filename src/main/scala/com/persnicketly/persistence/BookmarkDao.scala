package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.readability.model.{Article, Bookmark, User, UserData}

object BookmarkDao extends Dao {
  val collectionName = "bookmarks"

  // Initialize indexes
  collection.ensureIndex(MongoDBObject("article_id" -> 1, "user_id" -> 1))
  collection.ensureIndex(MongoDBObject("bookmark_id" -> 1))
  collection.ensureIndex(MongoDBObject("article_domain" -> 1,
                                       "article_procecced" -> 1,
                                       "update_date" -> 1,
                                       "favorite" -> 1))

  /**
   * Determine whether a user has a Bookmark for a given article
   * @param user to check for
   * @param article to check for
   * @return Some if a bookmark exists with user's userId and article's articleId
   */
  def get(user: User, article: Article): Option[Bookmark] = {
    user match {
      case User(_, _, _, _, _, Some(UserData(Some(uid), _, _, _))) =>
        collection.findOne(MongoDBObject("article_id" -> article.articleId, "user_id" -> uid)).map(Bookmark.apply)
      case _ => None
    }
  }

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
