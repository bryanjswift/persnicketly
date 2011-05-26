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
    val numPages = scala.math.ceil(meta.totalCount / pageSize).toInt
    val conditions = for (i <- 1 to numPages) yield BookmarkRequestConditions(i, pageSize, since, user)
    withChannel(config) { channel =>
      conditions.foreach(condition => {
        log.info("Adding {} to {} queue", condition, config.name)
        channel.basicPublish(exchange, config.name, config.message.properties, condition.toByteArray)
      })
      conditions
    }
  }

  def add(since: DateTime, user: User): Option[Seq[BookmarkRequestConditions]] = {
    val meta = Api.bookmarksMeta(Persnicketly.oauthConsumer, user, Some(since))
    addAll(meta, user, Some(since))
  }

  def processDelivery(delivery: Delivery): Boolean = {
    val conditions = BookmarkRequestConditions(delivery.getBody)
    val personalInfo = conditions.user.personalInfo.getOrElse(UserData.Empty)
    log.info("Processing page {} for {}", conditions.page.getOrElse(0), personalInfo.username)
    process(conditions)
  }

  def process(conditions: BookmarkRequestConditions): Boolean = {
    val bookmarks = Api.bookmarks(Persnicketly.oauthConsumer, conditions)
    val saved = bookmarks.map(mark => BookmarkDao.save(mark))
    saved.forall(_.id.isDefined)
  }
}
