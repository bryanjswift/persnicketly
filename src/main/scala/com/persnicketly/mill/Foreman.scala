package com.persnicketly.mill

import com.persnicketly.{Command,Persnicketly}
import com.persnicketly.persistence.UserDao

object Foreman extends Command {
  val group = new ThreadGroup("Mill")

  def start(): List[Thread] = {
    val config = Persnicketly.Config
    // start up consumers
    val userConsumer = async("User Consumer", UserQueue.startConsumer)
    val bookmarkRequestConsumer = async("Bookmark Reqeusts Consumer", BookmarkRequestsQueue.startConsumer)
    // re-add all existing users
    UserDao.all.foreach(UserQueue.add)
    // join consumers to main thread
    List(userConsumer, bookmarkRequestConsumer)
  }
}
