package com.persnicketly.mill

import com.persnicketly.Persnicketly
import com.persnicketly.readability.Api
import com.persnicketly.readability.api.BookmarkRequestConditions
import com.persnicketly.readability.model.{Meta,User,UserData}
import com.persnicketly.persistence.BookmarkDao
import org.joda.time.DateTime

object BookmarkRequestsQueue extends Queue {
  val queueName = "bookmarks-requests";

  def addAll(meta: Meta, user: User, since: Option[DateTime] = None): Option[Seq[BookmarkRequestConditions]] = {
    val pageSize = Persnicketly.Config("queue." + queueName + ".perpage").or(25)
    val numPages = scala.math.ceil(meta.totalCount.toDouble / pageSize).toInt
    log.debug("Found {} pages and {} total bookmarks to fetch", numPages, meta.totalCount)
    val conditions = for (i <- 1 to numPages) yield BookmarkRequestConditions(i, pageSize, since, user)
    withChannel(config) { channel =>
      conditions.foreach(condition => {
        log.info("Adding {} to {} queue", condition, queueName)
        channel.basicPublish(exchange, queueName, config.message.properties, condition.toByteArray)
        counter.inc()
      })
      conditions
    }
  }

  def add(since: DateTime, user: User): Option[Seq[BookmarkRequestConditions]] = {
    val meta = Api.Bookmarks.meta(Persnicketly.oauthConsumer, user, Some(since))
    meta.flatMap(m => addAll(m, user, Some(since)))
  }

  def processDelivery(delivery: Delivery): Boolean = {
    val conditions = BookmarkRequestConditions(delivery.getBody)
    val personalInfo = conditions.user.personalInfo.getOrElse(UserData.Empty)
    log.info("Processing page {} for {}", conditions.page.getOrElse(0), personalInfo.username)
    process(conditions)
  }

  def process(conditions: BookmarkRequestConditions): Boolean = {
    val bookmarks = Api.Bookmarks.fetch(Persnicketly.oauthConsumer, conditions)
    val saved = bookmarks.map(_.map(mark => BookmarkDao.save(mark))).getOrElse(List())
    saved.forall(_.id.isDefined)
    bookmarks.isDefined
  }
}
