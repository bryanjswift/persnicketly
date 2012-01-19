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
    var threads = List[Thread]()
    // start up consumers
    if (options.hasOption("mill")) {
      val articleConsumer = async("Article Save Consumer", ArticleQueue.startConsumer)
      val bookmarkRequestConsumer = async("Bookmark Reqeusts Consumer", BookmarkRequestsQueue.startConsumer)
      val userConsumer = async("User Consumer", UserQueue.startConsumer)
      threads = articleConsumer :: bookmarkRequestConsumer :: userConsumer :: threads
    }
    // Schedule tasks
    if (options.hasOption("scheduled")) {
      log.info("Scheduling Mill updates")
      executor.scheduleWithFixedDelay(new Runnable {
          override def run() {
            try {
              log.info("Processing users")
              Foreman.usersToUpdate.foreach(UserQueue.add)
            } catch {
              case e: Exception => log.error("Unable to compute scores", e)
              case _ => log.error("Mystery exception")
            }
          }
        }, 0L, 4L, TimeUnit.HOURS)
      executor.scheduleWithFixedDelay(new Runnable {
          override def run() {
            try {
              log.info("Computing recent scores")
              config("compute").or(Array(14)).foreach(c => BookmarkDao.compute(c))
              log.info("Finished with articles and scores")
            } catch {
              case e: Exception => log.error("Unable to compute scores", e)
              case _ => log.error("Mystery exception")
            }
          }
        }, 0L, 5L, TimeUnit.HOURS)
    }
    // join consumers to main thread
    threads
  }

  def usersToUpdate: List[User] = {
    val threshold = (new DateTime).minusHours(16)
    UserDao.all.filter(u => !u.lastProcessed.isDefined || u.lastProcessed.get.isBefore(threshold))
  }
}
