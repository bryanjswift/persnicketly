package com.persnicketly.redis

import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.codec.{RedisCodec, Utf8StringCodec}
import com.persnicketly.net.ServerAddress
import com.persnicketly.Persnicketly.Config

object RedisCluster {

  private val addresses = Config("redis.hosts").or(List(ServerAddress("localhost", 6379)))

  private val utf = new Utf8StringCodec()

  val clients = addresses.map(address => { address.redis })

  def using[K, V](codec: RedisCodec[K, V]) = master.using(codec)

  def master: Redis = {
    // Find potential masters
    val (masters, slaves) = clients.partition({ client =>
      val info = client.using(utf).exec(_.info).getOrElse("")
      info.contains("role:master")
    })

    // Select master and designate other potential masters as slaves
    val king = masters match {
      case master :: Nil => master
      case master :: newSlaves => {
        newSlaves.foreach({ slave => slave.using(utf).exec(_.slaveof(master.host, master.port)) })
        master
      }
      case Nil => {
        val master :: newSlaves = slaves
        newSlaves.foreach({ slave => slave.using(utf).exec(_.slaveof(master.host, master.port)) })
        master
      }
    }

    // Make sure slaves have the right master
    slaves.foreach({ slave =>
      val info = slave.using(utf).exec(_.info).getOrElse("")
      if (!(info.contains("master_host:" + king.host) || info.contains("master_port:" + king.port))) {
        slave.using(utf).exec(_.slaveof(king.host, king.port))
      }
    })

    // Return the selected master
    king
  }

}
