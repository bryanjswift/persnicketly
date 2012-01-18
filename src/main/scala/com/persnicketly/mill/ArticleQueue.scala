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
    withChannel(config) { channel =>
      log.info("Adding Article({}) to queue", article.articleId)
      channel.basicPublish(exchange, queueName, config.message.properties, article.toByteArray)
      counter.inc()
      article
    }
  }

  def processDelivery(delivery: Delivery): Boolean = {
    val article = Serializer[Article](delivery.getBody)
    log.info("Processing delivery of Article({})", article.articleId)
    ArticleDao.save(article)
    val retrieved = ArticleDao.get(article.articleId)
    counter.dec()
    retrieved == article
  }
}
