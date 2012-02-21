package com.persnicketly.redis

import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.codec.{RedisCodec, Utf8StringCodec}
import com.persnicketly.net.ServerAddress
import com.persnicketly.Logging
import com.persnicketly.Persnicketly.Config
import org.joda.time.DateTime
import scala.actors.Actor._

object RedisCluster extends Logging {

  private val addresses = Config("redis.hosts").or(List(ServerAddress("localhost", 6379)))

  private val utf = new Utf8StringCodec()

  val clients = addresses.map(address => { address.redis })

  private var lastElection: DateTime = new DateTime()
  private var elected: Redis = election

  private def election: Redis = {
    // Find potential masters
    val (masters, slaves) = clients.filter(_.isAlive).map(_.info).partition(_.role == "master")

    // Select master and designate other potential masters as slaves
    val (king, newSlaves) = masters match {
      case master :: Nil => (master, Nil)
      case master :: newSlaves => (master, newSlaves)
      case Nil => {
        val master :: newSlaves = slaves
        (master, newSlaves)
      }
    }

    // Make sure king is master
    if (king.role != "master") { king.redis.using(utf).exec(_.slaveofNoOne) }

    // Make sure slaves have the right master
    (newSlaves ::: slaves).filterNot(_ == king).foreach({ slave =>
      if (!(slave.masterHost == king.host || slave.masterPort == king.port)) {
        slave.redis.using(utf).exec(_.slaveof(king.host, king.port))
      }
    })

    // Return the selected master
    king.redis
  }

  case object Elect
  val elector = actor {
    loop {
      react {
        case Elect => {
          log.debug("* Elect received *")
          val now = new DateTime()
          if (lastElection.plusMinutes(3).isBefore(now)) {
            log.debug("** Electing new master **")
            elected = election
            lastElection = new DateTime()
          }
        }
        case _ => log.warn("Rogue messages to elector")
      }
    }
  }

  def master: Redis = {
    if (lastElection.plusMinutes(3).isBefore(new DateTime())) {
      elector ! Elect
    }
    elected
  }

  def using[K, V](codec: RedisCodec[K, V]) = master.using(codec)

}
