package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.Persnicketly.Config
import com.persnicketly.model.ScoredArticle
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

class ScoredArticleDao(from: Int) extends Dao {
  val collectionName = "scored_" + from

  val selectTimer = metrics.timer("scored_" + from + "-select")

  def select(count: Int): List[ScoredArticle] = {
    selectTimer.time {
      val articles = collection.find().limit(count).sort(BookmarkDao.scoredSort)
      articles.map(o => ScoredArticle(o.getAs[DBObject]("value").get)).toList
    }
  }

  def get(id: String): Option[ScoredArticle] = {
    collection.findOne(MongoDBObject("_id" -> id)).map(o => ScoredArticle(o.getAs[DBObject]("value").get))
  }
}

object ScoredArticleDao {
  private val daos = Config("compute").or(Array(14)).map(t => (t, new ScoredArticleDao(t))).toMap

  def select(from: Int, count: Int): List[ScoredArticle] = daos.get(from).get.select(count)
  def get(from: Int, id: String): Option[ScoredArticle] = daos.get(from).get.get(id)
}
