package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.{Logging, Persnicketly}
import com.persnicketly.model.ScoredArticle
import com.persnicketly.readability.model.Article
import org.scala_tools.time.Imports._
import org.bson.types.ObjectId
import org.joda.time.DateTime

class ScoredArticleDao extends Dao {
  import ScoredArticleDao._
  val collectionName = "articles"

  // DBObject types for getting aggregate data
  private val key = MongoDBObject("article_id" -> 1, "article_title" -> 1, "article_domain" -> 1, "article_url" -> 1, "article_excerpt" -> 1, "article_processed" -> 1)
  private val cond = "article_domain" $not ".*persnicket(ly.com|yapp.com|lyapp.com)".r + ("article_processed" -> true)
  private val initial = MongoDBObject("count" -> 0, "favorite_count" -> 0, "score" -> 0)
  private val reduce = """function(o,p) { if (o.favorite) { p.favorite_count++; p.score++; } p.count++; p.score++; }"""

  private val defaultSort = MongoDBObject("favorite_count" -> -1, "count" -> -1, "score" -> -1)

  // Initialize indexes
  collection.ensureIndex(MongoDBObject("article_id" -> 1))
  collection.ensureIndex(MongoDBObject("favorite_count" -> -1, "count" -> -1, "score" -> -1))

  private val articleConverter = dbobject2article _

  def all(): List[ScoredArticle] = {
    log.debug("Fetching all scored articles")
    val articles = collection.find().sort(MongoDBObject("score" -> -1))
    articles.map(articleConverter).toList
  }

  def find(count: Int): List[ScoredArticle] = {
    log.debug("Fetching the top {} articles", count)
    val articles = collection.find("score" $gt 2).limit(count).sort(defaultSort)
    articles.map(articleConverter).toList
  }

  def compute(): List[ScoredArticle] = {
    val bookmarks = new BookmarkDao
    val articles = bookmarks.collection.group(key, cond, initial, reduce)
    articles.map(articleConverter).toList
  }

  def get(articleId: String): Option[ScoredArticle] = {
    collection.findOne(MongoDBObject("article_id" -> articleId)).map(dbobject2article)
  }

  def recent(count: Int): List[ScoredArticle] = {
    val bookmarks = new BookmarkDao
    val now = (new DateTime) - 1.day
    val yesterday = now - 7.days
    val c = cond + ("update_date" -> MongoDBObject("$gt" -> yesterday, "$lt" -> now))
    val articles = bookmarks.collection.group(key, c, initial, reduce)
    articles.map(articleConverter).toList.sorted.take(count)
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
    builder += "article_domain" -> scored.article.domain
    builder += "article_url" -> scored.article.url
    builder += "article_processed" -> scored.article.processed
    scored.article.excerpt.foreach(ex => builder += ("article_excerpt" -> ex))
    builder += "favorite_count" -> scored.favoriteCount
    builder += "count" -> scored.count
    builder += "score" -> scored.score
    builder.result
  }

  implicit def dbobject2article(o: DBObject): ScoredArticle = {
    ScoredArticle(
      o._id,
      Article(
        o.getAsOrElse("article_id", ""),
        o.getAsOrElse("article_title", ""),
        o.getAsOrElse("article_domain", ""),
        o.getAsOrElse("article_url", ""),
        o.getAs[String]("article_excerpt"),
        o.getAsOrElse("article_processed", false)
      ),
      o.getAsOrElse("favorite_count", 0.0),
      o.getAsOrElse("count", 0.0)
    )
  }

  private def dao = { new ScoredArticleDao }

  def all() = dao.all()

  def compute() = dao.compute()

  def find(limit: Int) = dao.find(limit)

  def get(articleId: String) = dao.get(articleId)

  def recent(limit: Int) = dao.recent(limit)

  def save(scored: ScoredArticle) = dao.save(scored)

  def update(): Unit = {
    val instance = dao
    val articles = instance.compute
    articles.map(instance.save)
  }
}
