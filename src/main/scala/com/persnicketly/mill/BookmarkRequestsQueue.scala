package com.persnicketly.mill

import com.persnicketly.Persnicketly
import com.persnicketly.readability.api.BookmarkRequestConditions
import com.persnicketly.readability.model.{Meta,User}
import org.joda.time.DateTime

object BookmarkRequestsQueue extends Queue {
  val queueName = "bookmarks-requests";
  def addAll(meta: Meta, user: User): Option[Seq[BookmarkRequestConditions]] = {
    val perPage = Persnicketly.Config("queue." + queueName + ".perpage").or(25)
    val numPages = scala.math.ceil(meta.totalCount / perPage).toInt
    val conditions = for (i <- 1 to numPages) yield BookmarkRequestConditions(i, perPage, None, user)
    withChannel(config) { channel =>
      conditions.foreach(condition => {
        log.info("Adding {) to {} queue", condition, config.name)
        channel.basicPublish(exchange, config.name, config.message.properties, condition.toByteArray)
      })
      conditions
    }
  }
  def add(since: DateTime, user: User): Option[BookmarkRequestConditions] = {
    None
  }
}
