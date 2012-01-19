package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.readability.model.Article

object ArticleDao extends Dao {
  val collectionName = "articles"

  val saveTimer = metrics.timer("article-save")
  val articlesGauge = metrics.gauge("num-articles")(count)

  // Initialize indices
  collection.ensureIndex(MongoDBObject("article_id" -> 1), "article_id_1", true)

  def all(): List[Article] = {
    log.debug("Fetching all scored articles")
    val articles = collection.find()
    articles.map(Article.apply).toList
  }

  def count: Long = collection.count

  def get(articleId: String): Option[Article] = {
    collection.findOne(MongoDBObject("article_id" -> articleId)).map(Article.apply)
  }

  def save(article: Article): Article = {
    saveTimer.time {
      val query = MongoDBObject("article_id" -> article.articleId)
      log.info("Saving query -- {}", query)
      collection.update(query, article, upsert = true, multi = false)
      collection.findOne(query).get
    }
  }
}
