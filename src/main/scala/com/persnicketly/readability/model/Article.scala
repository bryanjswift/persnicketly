package com.persnicketly.readability.model

import org.joda.time.DateTime

case class Article(
  domain: String,
  title: String,
  url: String,
  excerpt: Option[String],
  word_count: Option[Int],
  id: String,
  author: Option[String],
  date_publish: Option[String],
  next_page_href: Option[String])

