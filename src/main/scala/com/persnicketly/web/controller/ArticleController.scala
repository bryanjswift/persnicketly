package com.persnicketly.web.controller

import com.persnicketly.{Logging, Persnicketly}
import com.persnicketly.mill.UserQueue
import com.persnicketly.persistence.{ScoredArticleDao, UserDao}
import com.persnicketly.readability.Api

object ArticleController extends Logging {
  def addArticleForUser(articleId: String, userId: Option[String]) = {
    UserDao.getById(userId.getOrElse("")) match {
      case Some(user) =>
        ScoredArticleDao.get(articleId) match {
          // Add article to reading list
          case Some(scored) => {
            Api.bookmark(Persnicketly.oauthConsumer, user, scored.article)
            UserQueue.add(user)
          }
          // show article not found page
          case None => log.info("No article found with article_id '{}'", articleId)
        }
      case None => log.info("No logged in user, just redirecting")
    }
  }
}
