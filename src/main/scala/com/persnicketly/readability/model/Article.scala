package com.persnicketly.readability.model

import org.joda.time.DateTime

case class Article(
  id: Int,
  author: String,
  content: String,
  content_size: Int,
  date_publish: DateTime,
  domain: String,
  next_page_href: Option[String],
  short_url: String,
  title: String,
  url: String)

