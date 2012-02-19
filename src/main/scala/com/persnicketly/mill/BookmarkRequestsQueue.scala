package com.persnicketly.mill

import com.persnicketly.Persnicketly
import com.persnicketly.readability.Api
import com.persnicketly.readability.api.BookmarkRequestConditions
import com.persnicketly.readability.model.{Meta,User,UserData}
import com.persnicketly.redis.BookmarkRequestConditionsCodec
import com.persnicketly.persistence.BookmarkDao
import org.joda.time.DateTime

object BookmarkRequestsQueue extends RedisQueue[BookmarkRequestConditions] {

  def queueName = "bookmarks-requests"

  val codec = new BookmarkRequestConditionsCodec()

  def addAll(meta: Meta, user: User, since: Option[DateTime] = None): Seq[BookmarkRequestConditions] = {
    val pageSize = Persnicketly.Config("queue." + queueName + ".perpage").or(25)
    val numPages = scala.math.ceil(meta.totalCount.toDouble / pageSize).toInt
    log.debug("Found {} pages and {} total bookmarks to fetch", numPages, meta.totalCount)
    val conditions = for (i <- 1 to numPages) yield BookmarkRequestConditions(i, pageSize, since, user)
    conditions.foreach(condition => {
      log.debug("Adding {} to {} queue", condition, queueName)
      publish(condition)
    })
    conditions
  }

  def add(since: DateTime, user: User): Option[Seq[BookmarkRequestConditions]] = {
    val meta = Api.Bookmarks.meta(user, Some(since))
    meta.map(m => addAll(m, user, Some(since)))
  }

  def process(conditions: BookmarkRequestConditions): Boolean = {
    val personalInfo = conditions.user.personalInfo.getOrElse(UserData.Empty)
    log.info("Processing page {} for {}", conditions.page.getOrElse(0), personalInfo.username)
    val bookmarks = Api.Bookmarks.fetch(conditions)
    val saved = bookmarks.map(_.map(mark => BookmarkDao.save(mark))).getOrElse(List())
    saved.forall(_.id.isDefined)
    bookmarks.isDefined
  }
}
