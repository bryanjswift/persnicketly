package com.persnicketly.readability.model

case class Article(
  articleId: String,
  title: String,
  domain: String,
  url: String,
  excerpt: Option[String],
  processed: Boolean)

