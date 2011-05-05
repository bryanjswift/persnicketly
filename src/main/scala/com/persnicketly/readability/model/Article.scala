package com.persnicketly.readability.model

case class Article(
  articleId: String,
  title: String,
  url: String,
  excerpt: Option[String])

