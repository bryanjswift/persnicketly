package com.persnicketly.mill

import com.persnicketly.{Command,Persnicketly}
import com.persnicketly.persistence.UserDao
import com.persnicketly.readability.model.User
import org.joda.time.DateTime

object Foreman extends Command {
  val group = new ThreadGroup("Mill")

  def start(options: CliOpts): List[Thread] = {
    val config = Persnicketly.Config
    // start up consumers
    val userConsumer = async("User Consumer", UserQueue.startConsumer)
    val bookmarkRequestConsumer = async("Bookmark Reqeusts Consumer", BookmarkRequestsQueue.startConsumer)
    // re-add all existing users
    usersToUpdate.foreach(UserQueue.add)
    // join consumers to main thread
    List(userConsumer, bookmarkRequestConsumer)
  }

  def usersToUpdate: List[User] = {
    val threshold = (new DateTime).minusHours(16)
    UserDao.all.filter(u => !u.lastProcessed.isDefined || u.lastProcessed.get.isBefore(threshold))
  }
}
