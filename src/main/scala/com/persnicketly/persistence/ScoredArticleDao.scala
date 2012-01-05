package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.Persnicketly.Config
import com.persnicketly.model.ScoredArticle
import org.scala_tools.time.Imports._
import org.joda.time.DateTime

object ScoredArticleDao extends Dao {
  val collectionName = "articles"

  val scored = Connection.mongo(Config("db.name").or("persnicketly_test"))("scored")

  val computeTimer = metrics.timer("articles-compute")
  val recentTimer = metrics.timer("articles-recent")
  val saveTimer = metrics.timer("articles-save")

  // DBObject types for getting aggregate data
  private val key = MongoDBObject("article_id" -> 1, "article_title" -> 1, "article_domain" -> 1, "article_url" -> 1, "article_excerpt" -> 1, "article_processed" -> 1)
  private val cond = MongoDBObject("article_processed" -> true)
  private val initial = MongoDBObject("count" -> 0, "favorite_count" -> 0, "score" -> 0)
  private val reduce = """function(o,p) { if (o.favorite) { p.favorite_count++; p.score++; } p.count++; p.score++; }"""

  private val m = """
  function map() {
    var a = {article_id:this.article_id, article_title:this.article_title, article_domain:this.article_domain, article_url:this.article_url, article_excerpt:this.article_excerpt, article_processed:this.article_processed, favorite_count:this.favorite ? 1 : 0, count:1};
    emit(this.article_id, a);
  }"""
  private val r = """
  function reduce(key, values) {
    var result = {article_id:"", article_title:"", article_domain:"", article_url:"", article_excerpt:"", article_processed:false, favorite_count:0, count:0, article_id:key};
    values.forEach(function (a) {result.article_title = a.article_title;result.article_domain = a.article_domain;result.article_url = a.article_url;result.article_excerpt = a.article_excerpt;result.favorite_count += a.favorite_count;result.count += a.count;});
    return result;
  }"""
  private val f ="""
  function (k, v) {
    v.score = v.favorite_count + v.count;
    return v;
  }"""
  private val out = MapReduceReduceOutput("scored")
  private val q = ("article_domain" $not ".*persnicket(ly.com|yapp.com|lyapp.com)".r) + ("article_processed" -> true)
  private val s = MongoDBObject("article_id" -> 1)
  private val c = MapReduceCommand("bookmarks", m, r, out, query = Some(q), sort = Some(s), finalizeFunction = Some(f))

  private val defaultSort = MongoDBObject("favorite_count" -> -1, "count" -> -1, "score" -> -1)
  private val scoredSort = MongoDBObject("value.favorite_count" -> -1, "value.count" -> -1, "value.score" -> -1)

  // Initialize indices
  collection.ensureIndex(MongoDBObject("article_id" -> 1))
  collection.ensureIndex(defaultSort)
  scored.ensureIndex(scoredSort)

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
      BookmarkDao.collection.mapReduce(c)

      val bookmarks = BookmarkDao.collection
      val articles = bookmarks.group(key, cond, initial, reduce)
      articles.map(ScoredArticle.apply).toList
    }
  }

  def get(articleId: String): Option[ScoredArticle] = {
    collection.findOne(MongoDBObject("article_id" -> articleId)).map(ScoredArticle.apply)
  }

  def mr(count: Int): List[ScoredArticle] = {
    val articles = scored.find().limit(count).sort(scoredSort).map(_.getAs[DBObject]("value").get)
    articles.map(ScoredArticle.apply).toList
  }

  def recent(count: Int): List[ScoredArticle] = {
    val now = new DateTime
    recent(count, now - 7.days, now - 1.day)
  }

  def recent(count: Int, since: DateTime, until: DateTime): List[ScoredArticle] = {
    recentTimer.time {
      val bookmarks = BookmarkDao.collection
      val c = cond + ("update_date" -> MongoDBObject("$gt" -> since, "$lt" -> until))
      val articles = bookmarks.group(key, c, initial, reduce)
      articles.map(ScoredArticle.apply).toList.sorted.take(count)
    }
  }

  def save(scored: ScoredArticle): ScoredArticle = {
    saveTimer.time {
      val query = scored.id match {
        case Some(id) => MongoDBObject("_id" -> id)
        case None => MongoDBObject("article_id" -> scored.article.articleId)
      }
      collection.update(query, scored, upsert = true, multi = false)
      collection.findOne(query).get
    }
  }

  def update() {
    val articles = compute()
    articles.foreach(save)
  }
}
