package com.persnicketly.mill

import com.persnicketly.Persnicketly
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.User
import com.persnicketly.redis.ObjectIdCodec
import org.bson.types.ObjectId
import org.joda.time.DateTime

object UserQueue extends RedisQueue[ObjectId] {

  def queueName = "new-users"

  val codec = new ObjectIdCodec()

  def add(user: User): Option[ObjectId] = {
    if (user.id.isDefined) {
      log.info("Adding User({}) to queue", user.id.get)
      publish(user.id.get)
    } else {
      None
    }
  }

  def process(id: ObjectId): Boolean = {
    log.info("Processing delivery of {}", id)
    UserDao.get(id).map(user => {
      val meta = Api.Bookmarks.meta(user, user.lastProcessed)
      val added = meta.map(m => BookmarkRequestsQueue.addAll(m, user, user.lastProcessed))
      if (added.isDefined) { UserDao.save(user.copy(lastProcessed = Some(new DateTime))) }
      added.isDefined
    }).getOrElse(false)
  }
}
