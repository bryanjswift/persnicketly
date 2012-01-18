package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.Persnicketly.Config
import com.persnicketly.model.ScoredArticle
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

class ScoredArticleDao(from: Int) extends Dao {
  val collectionName = "scored_" + from

  val selectTimer = metrics.timer("scored_" + from + "-select")

  def select(from: Int, count: Int): List[ScoredArticle] = {
    selectTimer.time {
      val collection = db("scored_" + from)
      val articles = collection.find().limit(count).sort(BookmarkDao.scoredSort)
      articles.map(o => ScoredArticle(o.getAs[DBObject]("value").get)).toList
    }
  }
}

object ScoredArticleDao {
  private val daos = Map(14 -> new ScoredArticleDao(14), 60 -> new ScoredArticleDao(60))

  def select(from: Int, count: Int): List[ScoredArticle] = daos.get(from).get.select(from, count)
}
