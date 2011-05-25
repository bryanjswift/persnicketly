package com.persnicketly.mill

import com.persnicketly.Persnicketly
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.User
import org.bson.types.ObjectId

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

  def processDelivery(delivery: Delivery): Boolean = {
    val id = new ObjectId(delivery.getBody)
    val user = UserDao.get(id)
    user.map(process).getOrElse(false)
  }

  def process(user: User): Boolean = {
    val meta = Api.bookmarksMeta(Persnicketly.oauthConsumer, user)
    val added = BookmarkRequestsQueue.addAll(meta, user)
    added.isDefined
  }
}
