package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.model.ScoredArticle
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object ScoredArticleDao extends Dao {
  val collectionName = "articles"

  val computeTimer = metrics.timer("articles-compute")
  val recentTimer = metrics.timer("articles-recent")

  // DBObject types for getting aggregate data
  private val key = MongoDBObject("article_id" -> 1, "article_title" -> 1, "article_domain" -> 1, "article_url" -> 1, "article_excerpt" -> 1, "article_processed" -> 1)
  private val cond = "article_domain" $not ".*persnicket(ly.com|yapp.com|lyapp.com)".r + ("article_processed" -> true)
  private val initial = MongoDBObject("count" -> 0, "favorite_count" -> 0, "score" -> 0)
  private val reduce = """function(o,p) { if (o.favorite) { p.favorite_count++; p.score++; } p.count++; p.score++; }"""

  private val defaultSort = MongoDBObject("favorite_count" -> -1, "count" -> -1, "score" -> -1)

  // Initialize indexes
  collection.ensureIndex(MongoDBObject("article_id" -> 1))
  collection.ensureIndex(MongoDBObject("favorite_count" -> -1, "count" -> -1, "score" -> -1))

  def all(): List[ScoredArticle] = {
    log.debug("Fetching all scored articles")
    val articles = collection.find().sort(MongoDBObject("score" -> -1))
    articles.map(ScoredArticle.apply).toList
  }

  def find(count: Int): List[ScoredArticle] = {
    log.debug("Fetching the top {} articles", count)
    val articles = collection.find("score" $gt 2).limit(count).sort(defaultSort)
    articles.map(ScoredArticle.apply).toList
  }

  def compute(): List[ScoredArticle] = {
    computeTimer.time {
      val bookmarks = BookmarkDao.collection
      val articles = bookmarks.group(key, cond, initial, reduce)
      articles.map(ScoredArticle.apply).toList
    }
  }

  def get(articleId: String): Option[ScoredArticle] = {
    collection.findOne(MongoDBObject("article_id" -> articleId)).map(ScoredArticle.apply)
  }

  def recent(count: Int): List[ScoredArticle] = {
    recentTimer.time {
      val bookmarks = BookmarkDao.collection
      val now = (new DateTime) - 1.day
      val yesterday = now - 7.days
      val c = cond + ("update_date" -> MongoDBObject("$gt" -> yesterday, "$lt" -> now))
      val articles = bookmarks.group(key, c, initial, reduce)
      articles.map(ScoredArticle.apply).toList.sorted.take(count)
    }
  }

  def save(scored: ScoredArticle): ScoredArticle = {
    val query = scored.id match {
      case Some(id) => MongoDBObject("_id" -> id)
      case None => MongoDBObject("article_id" -> scored.article.articleId)
    }
    collection.update(query, scored, upsert = true, multi = false)
    collection.findOne(query).get
  }

  def update(): Unit = {
    val articles = compute
    articles.foreach(save)
  }
}
