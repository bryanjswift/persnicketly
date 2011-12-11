package com.persnicketly.mill

import com.persnicketly.Persnicketly
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.User
import org.bson.types.ObjectId
import org.joda.time.DateTime

object UserQueue extends Queue {
  val queueName = "new-users";

  def add(user: User): Option[User] = {
    withChannel(config) { channel =>
      if (user.id.isDefined) {
        log.info("Adding User({}) to queue", user.id.get)
        channel.basicPublish(exchange, config.name, config.message.properties, user.id.get.toByteArray)
        counter += 1
      }
      user
    }
  }

  def processDelivery(delivery: Delivery): Boolean = {
    val id = new ObjectId(delivery.getBody)
    val user = UserDao.get(id)
    log.info("Processing delivery of {}", id)
    user.map(process).getOrElse(false)
  }

  def process(user: User): Boolean = {
    val meta = Api.Bookmarks.meta(Persnicketly.oauthConsumer, user, user.lastProcessed)
    val added = meta.flatMap(m => BookmarkRequestsQueue.addAll(m, user, user.lastProcessed))
    UserDao.save(user.copy(lastProcessed = Some(new DateTime)))
    counter -= 1
    added.isDefined
  }
}
