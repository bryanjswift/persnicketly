package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.map_reduce.MapReduceStandardOutput
import com.persnicketly.IOUtils
import com.persnicketly.mill.ArticleQueue
import com.persnicketly.readability.model.{Article, Bookmark, User, UserData}
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object BookmarkDao extends Dao {
  val collectionName = "bookmarks"

  val computeTimer = metrics.timer("bookmark-compute")

  // Initialize indexes
  collection.ensureIndex(MongoDBObject("article_id" -> 1, "user_id" -> 1))
  collection.ensureIndex(MongoDBObject("bookmark_id" -> 1))
  collection.ensureIndex(MongoDBObject("article_processed" -> 1,
                                       "update_date" -> 1))

  val scoredSort = MongoDBObject("value.favorite_count" -> -1, "value.count" -> -1, "value.score" -> -1)

  val m = IOUtils.read("mapreduce/bookmark-map.js")
  val r = IOUtils.read("mapreduce/bookmark-reduce.js")
  val q = MongoDBObject("article_processed" -> true)
  val s = MongoDBObject("article_id" -> 1)
  val f = IOUtils.read("mapreduce/bookmark-finalize.js")

  private def command(out: MapReduceStandardOutput, q: MongoDBObject) =
    MapReduceCommand("bookmarks", m, r, out, query = Some(q), sort = Some(s), finalizeFunction = Some(f))

  def compute(numDays: Int) {
    computeTimer.time {
      val scored = db("scored_" + numDays)
      scored.ensureIndex(scoredSort)
      val out = MapReduceStandardOutput(scored.name)
      val until = new DateTime
      val since = until - numDays.days
      log.info("Computing scores for {} through {}", since, until)
      val query = q ++ ("update_date" -> MongoDBObject("$gt" -> since, "$lt" -> until))
      collection.mapReduce(command(out, query)).errorMessage.foreach(m => {
        log.error("MapReduce failed with message '{}'", m)
      })
    }
  }

  def count: Long = collection.count

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
    ArticleQueue.add(bookmark)
    collection.update(query, bookmark, upsert = true, multi = false)
    collection.findOne(query).get
  }
}
