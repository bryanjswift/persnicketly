package com.persnicketly.mill

import com.persnicketly.{Command,Persnicketly}
import com.persnicketly.persistence.{BookmarkDao, ScoredArticleDao, UserDao}
import com.persnicketly.readability.model.User
import org.joda.time.DateTime
import java.util.concurrent.{ScheduledThreadPoolExecutor, TimeUnit}

object Foreman extends Command {
  val group = new ThreadGroup("Mill")
  val executor = new ScheduledThreadPoolExecutor(2)

  def start(options: CliOpts): List[Thread] = {
    val config = Persnicketly.Config
    // start up consumers
    val userConsumer = async("User Consumer", UserQueue.startConsumer)
    val bookmarkRequestConsumer = async("Bookmark Reqeusts Consumer", BookmarkRequestsQueue.startConsumer)
    // Schedule tasks
    if (options.hasOption("scheduled")) {
      log.info("Scheduling Mill updates")
      executor.scheduleWithFixedDelay(new Runnable {
          override def run() {
            log.info("Processing users")
            Foreman.usersToUpdate.foreach(UserQueue.add)
          }
        }, 0L, 4L, TimeUnit.HOURS)
      executor.scheduleWithFixedDelay(new Runnable {
          override def run() {
            log.info("Updating article scores")
            ScoredArticleDao.update()
            Array(14, 30, 60).foreach(c => BookmarkDao.compute(c))
          }
        }, 0L, 5L, TimeUnit.HOURS)
    }
    // join consumers to main thread
    List(userConsumer, bookmarkRequestConsumer)
  }

  def usersToUpdate: List[User] = {
    val threshold = (new DateTime).minusHours(16)
    UserDao.all.filter(u => !u.lastProcessed.isDefined || u.lastProcessed.get.isBefore(threshold))
  }
}
