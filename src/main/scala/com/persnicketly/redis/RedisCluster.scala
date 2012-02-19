package com.persnicketly.redis

import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.codec.{RedisCodec, Utf8StringCodec}
import com.persnicketly.net.ServerAddress
import com.persnicketly.Persnicketly.Config

object RedisCluster {

  private val addresses = Config("redis.hosts").or(List(ServerAddress("localhost", 6379)))

  private val utf = new Utf8StringCodec()

  val clients = addresses.map(address => { address.redis })

  def in[K, V, T](codec: RedisCodec[K, V])(thunk: RedisConnection[K, V] => T): Option[T] = master.in(codec)(thunk)

  def master: Redis = {
    val (masters, slaves) = clients.partition({ client =>
      val info = client.in(utf)(_.info).getOrElse("")
      info.contains("role:master")
    })
    masters match {
      case master :: Nil => master
      case master :: newSlaves => {
        newSlaves.foreach({ slave => slave.in(utf)(_.slaveof(master.host, master.port)) })
        master
      }
      case Nil => {
        val master :: newSlaves = slaves
        newSlaves.foreach({ slave => slave.in(utf)(_.slaveof(master.host, master.port)) })
        master
      }
    }
  }

}
