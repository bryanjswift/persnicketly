package com.persnicketly.readability.model

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

case class Article(
  articleId: String,
  title: String,
  domain: String,
  url: String,
  excerpt: Option[String],
  processed: Boolean,
  published: Option[DateTime],
  content: Option[String],
  size: Option[Int],
  nextPage: Option[String]) {

  val publishedDisplay = published.map(_.toString("MM/dd/yyyy"))
}

object Article {
  def apply(
    articleId: String,
    title: String,
    domain: String,
    url: String,
    excerpt: Option[String],
    processed: Boolean): Article = Article(articleId, title, domain, url, excerpt, processed, None, None, None, None)

  def apply(o: DBObject): Article = {
    Article(
      o.getAsOrElse("article_id", ""),
      o.getAsOrElse("article_title", ""),
      o.getAsOrElse("article_domain", ""),
      o.getAsOrElse("article_url", ""),
      o.getAs[String]("article_excerpt"),
      o.getAsOrElse("article_processed", false)
    )
  }

  implicit def article2dbobject(article: Article): MongoDBObject = {
    val builder = MongoDBObject.newBuilder
    builder += "article_id" -> article.articleId
    builder += "article_title" -> article.title
    builder += "article_domain" -> article.domain
    builder += "article_url" -> article.url
    builder += "article_excerpt" -> article.excerpt
    builder += "article_processed" -> article.processed
    builder.result
  }
}
