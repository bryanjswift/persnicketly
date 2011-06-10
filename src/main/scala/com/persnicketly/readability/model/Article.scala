package com.persnicketly.readability.model

import org.joda.time.DateTime

case class Article(
  articleId: String,
  title: String,
  domain: String,
  url: String,
  excerpt: Option[String],
  processed: Boolean,
  published: Option[DateTime]) {

  val publishedDisplay = published.map(_.toString("MM/dd/yyyy"))
}

object Article {
  def apply(
    articleId: String,
    title: String,
    domain: String,
    url: String,
    excerpt: Option[String],
    processed: Boolean): Article = Article(articleId, title, domain, url, excerpt, processed, None)
}
