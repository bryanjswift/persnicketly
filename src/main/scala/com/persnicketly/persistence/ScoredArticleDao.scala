package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.{Logging, Persnicketly}
import com.persnicketly.model.ScoredArticle
import com.persnicketly.readability.model.Article
import org.bson.types.ObjectId

class ScoredArticleDao extends Dao {
  import ScoredArticleDao._
  val collectionName = "articles"

  // DBObject types for getting aggregate data
  private val key = MongoDBObject("article_id" -> 1, "favorite" -> 1, "article_title" -> 1, "article_url" -> 1, "article_excerpt" -> 1)
  private val cond = MongoDBObject()
  private val initial = MongoDBObject("score" -> 0)
  private val reduce = "function(o,p) { if (o.favorite) { p.score += 2; } else { p.score += 1; } }"

  def all(): List[ScoredArticle] = {
    val bookmarkDao = new BookmarkDao
    val articles = bookmarkDao.collection.group(key, cond, initial, reduce)
    articles.map(dbobject2article).toList
  }

  def save(scored: ScoredArticle): ScoredArticle = {
    val query = scored.id match {
      case Some(id) => MongoDBObject("_id" -> id)
      case None => MongoDBObject("article_id" -> scored.article.articleId)
    }
    collection.update(query, scored, upsert = true, multi = false)
    collection.findOne(query).get
  }
}

object ScoredArticleDao {
  implicit def article2dbobject(scored: ScoredArticle): DBObject = {
    val builder = MongoDBObject.newBuilder
    scored.id.foreach(id => builder += ("_id" -> id))
    builder += "article_id" -> scored.article.articleId
    builder += "article_title" -> scored.article.title
    builder += "article_url" -> scored.article.url
    scored.article.excerpt.foreach(ex => builder += ("article_excerpt" -> ex))
    builder += "score" -> scored.score
    builder.result
  }
  implicit def dbobject2article(o: DBObject): ScoredArticle = {
    ScoredArticle(
      o._id,
      Article(
        o.getAsOrElse("article_id", ""),
        o.getAsOrElse("article_title", ""),
        o.getAsOrElse("article_url", ""),
        o.getAs[String]("article_excerpt")
      ),
      o.getAsOrElse("score", 0.0)
    )
  }
}
