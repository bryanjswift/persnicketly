package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.Persnicketly.Config
import com.persnicketly.model.ScoredArticle
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object ScoredArticleDao extends Dao {
  val collectionName = "articles"

  val saveTimer = metrics.timer("articles-save")
  val selectTimer = metrics.timer("articles-select")

  val defaultSort = MongoDBObject("favorite_count" -> -1, "count" -> -1, "score" -> -1)
  val scoredSort = MongoDBObject("value.favorite_count" -> -1, "value.count" -> -1, "value.score" -> -1)

  // Initialize indices
  collection.ensureIndex(MongoDBObject("article_id" -> 1))

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

  def get(articleId: String): Option[ScoredArticle] = {
    collection.findOne(MongoDBObject("article_id" -> articleId)).map(ScoredArticle.apply)
  }

  def select(from: Int, count: Int): List[ScoredArticle] = {
    selectTimer.time {
      val collection = db("scored_" + from)
      val articles = collection.find().limit(count).sort(scoredSort)
      articles.map(o => ScoredArticle(o.getAs[DBObject]("value").get)).toList
    }
  }

  def save(scored: ScoredArticle): ScoredArticle = {
    saveTimer.time {
      val query = scored.id match {
        case Some(id) => MongoDBObject("_id" -> id)
        case None => MongoDBObject("article_id" -> scored.article.articleId)
      }
      log.info("Saving query -- {}", query)
      collection.update(query, scored, upsert = true, multi = false)
      collection.findOne(query).get
    }
  }
}
