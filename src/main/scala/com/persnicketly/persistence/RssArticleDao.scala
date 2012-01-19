package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.model.RssArticle
import org.joda.time.DateTime

object RssArticleDao extends Dao {
  val collectionName = "rss_articles"

  // Initialize indices
  collection.ensureIndex(MongoDBObject("r" -> 1))

  val maxRank = java.lang.Integer.MAX_VALUE

  def all: List[RssArticle] = collection.find().map(RssArticle.apply).toList

  def get(articleId: String): Option[RssArticle] = {
    collection.findOne(MongoDBObject("_id" -> articleId)).map(RssArticle.apply)
  }

  def save(article: RssArticle): RssArticle = {
    val query = MongoDBObject("_id" -> article.id)
    val o: DBObject = article
    log.debug("Saving -- {}", o)
    collection.update(query, article, upsert = true, multi = false)
    collection.findOne(query).get
  }

  def update(): List[RssArticle] = {
    var existing = all.map(r => (r.id, RssArticle(r.id, None, r.rank, r.updated))).toMap
    val articles = ScoredArticleDao.select(from = 60, count = 100).map(_.article)
    var i = 0;
    val now = new DateTime
    articles.foreach(a => {
      i = i + 1
      val id = a.articleId
      val raw = existing.getOrElse(Some(id), RssArticle(Some(id), None, None, Some(now))).copy(rank = Some(i))
      val article = 
        if (raw.rank.isDefined && raw.lastRank.isDefined && raw.rank.get < 10 && raw.lastRank.get > 10) {
          raw.copy(updated = Some(now))
        } else {
          raw
        }
      existing += (Some(id) -> article)
    })
    existing.values.map(save).toList
  }
}

