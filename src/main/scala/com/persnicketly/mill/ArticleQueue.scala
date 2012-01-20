package com.persnicketly.mill

import com.persnicketly.{Persnicketly, Serializer}
import com.persnicketly.persistence.ArticleDao
import com.persnicketly.readability.model.{Bookmark, Article}

object ArticleQueue extends Queue {
  val queueName = "articles";

  def add(bookmark: Bookmark): Option[Bookmark] = {
    val result = add(bookmark.article)
    if (result.isDefined) { Some(bookmark) }
    else { None}
  }

  def add(article: Article): Option[Article] = {
    log.debug("Adding Article({}) to queue", article.articleId)
    publish(article.toByteArray, article)
  }

  def processDelivery(delivery: Delivery): Boolean = {
    val article = Serializer[Article](delivery.getBody)
    log.debug("Processing delivery of Article({})", article.articleId)
    ArticleDao.save(article)
    val retrieved = ArticleDao.get(article.articleId)
    retrieved.isDefined
  }
}
