package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.Constants
import com.persnicketly.Persnicketly.Config
import com.persnicketly.model.RssArticle
import org.joda.time.DateTime

class RssArticleDao(days: Int) extends Dao {
  val collectionName = "rss_articles_" + days

  val defaultSort = MongoDBObject("r" -> 1)

  // Initialize indices
  collection.ensureIndex(defaultSort)

  val maxRank = java.lang.Integer.MAX_VALUE

  def all: List[RssArticle] = collection.find().sort(defaultSort).map(RssArticle.apply).toList

  def get(articleId: String): Option[RssArticle] =
    collection.findOne(MongoDBObject("_id" -> articleId)).map(RssArticle.apply)

  def save(article: RssArticle): RssArticle = {
    val query = MongoDBObject("_id" -> article.id)
    val o: DBObject = article
    log.debug("Saving -- {}", o)
    collection.update(query, article, upsert = true, multi = false)
    collection.findOne(query).get
  }

  def select(count: Int): List[RssArticle] =
    collection.find("r" $lte count).sort(defaultSort).map(RssArticle.apply).toList

  def update(): List[RssArticle] = {
    var existing = all.map(r => (r.id, RssArticle(r.id, None, r.rank, r.updated, r.scored))).toMap
    val articles = ScoredArticleDao.select(from = days, count = 100).map(_.article)
    var i = 0;
    val now = new DateTime
    articles.foreach(a => {
      i = i + 1
      val id = a.articleId

      val raw: RssArticle =
        existing.getOrElse(Some(id), RssArticle(Some(id), Some(now))).copy(rank = Some(i))

      val article =
        if (raw.rank.isDefined && raw.lastRank.isDefined && raw.rank.get < 10 && raw.lastRank.get > 10) {
          raw.copy(updated = Some(now))
        } else {
          raw
        }
      existing += (Some(id) -> article)
    })
    Cache.put(Constants.RssUpdated, now)
    existing.values.map(save).toList
  }
}

object RssArticleDao {
  private val daos = Config("compute").or(Array(14)).map(t => (t, new RssArticleDao(t))).toMap

  def all(days: Int) = daos.get(days).get.all

  def get(days: Int, articleId: String) = daos.get(days).get.get(articleId)

  def save(days: Int, article: RssArticle) = daos.get(days).get.save(article)

  def select(days: Int, count: Int) = daos.get(days).get.select(count)

  def update() { daos.values.foreach(dao => dao.update()) }
}
