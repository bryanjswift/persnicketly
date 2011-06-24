package com.persnicketly.model

import com.mongodb.casbah.Imports._
import com.persnicketly.readability.model.Article
import org.bson.types.ObjectId

case class ScoredArticle(id: Option[ObjectId],
                         article: Article,
                         favoriteCount: Double,
                         count: Double,
                         isBookmarked: Boolean,
                         isFavorited: Boolean) extends Ordered[ScoredArticle] {

  val numFavorites = favoriteCount.toInt
  val numBookmarks = count.toInt
  val score = numFavorites + numBookmarks

  def compare(that: ScoredArticle): Int = {
    if (that.numFavorites != this.numFavorites) { that.numFavorites.compareTo(this.numFavorites) }
    else if (that.numBookmarks != this.numBookmarks) { that.numBookmarks.compareTo(this.numBookmarks) }
    else { that.article.articleId.compareTo(this.article.articleId) }
  }
}

object ScoredArticle {
  def apply(id: Option[ObjectId], article: Article, favoriteCount: Double, count: Double): ScoredArticle =
    new ScoredArticle(id, article, favoriteCount, count, false, false)

  implicit def apply(o: DBObject): ScoredArticle = {
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
}
