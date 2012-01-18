package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.Persnicketly.Config
import com.persnicketly.model.ScoredArticle
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object ArticleDao extends Dao {
  val collectionName = "articles"

  val saveTimer = metrics.timer("articles-save")

  // Initialize indices
  collection.ensureIndex(MongoDBObject("article_id" -> 1))

  def all(): List[ScoredArticle] = {
    log.debug("Fetching all scored articles")
    val articles = collection.find().sort(MongoDBObject("score" -> -1))
    articles.map(ScoredArticle.apply).toList
  }

  def get(articleId: String): Option[ScoredArticle] = {
    collection.findOne(MongoDBObject("article_id" -> articleId)).map(ScoredArticle.apply)
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
