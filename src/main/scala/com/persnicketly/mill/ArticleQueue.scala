package com.persnicketly.mill

import com.persnicketly.{Persnicketly, Serializer}
import com.persnicketly.persistence.ArticleDao
import com.persnicketly.readability.model.{Bookmark, Article}

object ArticleQueue extends RedisQueue[Article] {
  val queueName = "articles"
  def parser = Parse(x => Article(x))

  def add(bookmark: Bookmark): Option[Bookmark] =
    add(bookmark.article).map(_ => bookmark)

  def add(article: Article): Option[Article] = {
    log.debug("Adding Article({}) to queue", article.articleId)
    publish(article)
  }

  def process(article: Article): Boolean = {
    log.debug("Processing delivery of Article({})", article.articleId)
    ArticleDao.save(article)
    val retrieved = ArticleDao.get(article.articleId)
    retrieved.isDefined
  }
}
