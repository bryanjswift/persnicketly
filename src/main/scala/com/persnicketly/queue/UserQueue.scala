package com.persnicketly.queue

import com.persnicketly.Persnicketly
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.User

object UserQueue extends Queue {
  val queueName = "new-users";
  def add(user: User): Option[User] = {
    withChannel(config) { channel =>
      if (user.id.isDefined) {
        log.info("Adding User({}) to queue", user.id.get)
        channel.basicPublish(exchange, config.name, config.message.properties, user.id.get.toByteArray)
      }
      user
    }
  }
  def process(user: User): Unit = {
    val meta = Api.bookmarksMeta(Persnicketly.oauthConsumer, user)
    BookmarkRequestsQueue.addAll(meta, user)
  }
}
