package com.persnicketly.redis

import com.lambdaworks.redis.{RedisClient, RedisConnection}
import com.lambdaworks.redis.codec.RedisCodec
import com.persnicketly.net.ServerAddress
import com.persnicketly.Logging

case class Redis(host: String, port: Int) extends Logging {

  def this(address: ServerAddress) = this(address.host, address.port)

  val client = new RedisClient(host, port)

  def connect[K, V](codec: RedisCodec[K, V]): Option[RedisConnection[K, V]] = {
    try { Some(client.connect(codec)) }
    catch {
      case e: Exception => {
        log.warn("Unable to connect to {}", this)
        None
      }
    }
  }

  def in[K, V, T](codec: RedisCodec[K, V])(thunk: RedisConnection[K, V] => T): Option[T] = {
    connect(codec).map({ connection =>
      try { thunk(connection) } finally { connection.close }
    })
  }

}
