package com.persnicketly.redis

import com.lambdaworks.redis.{RedisClient, RedisConnection}
import com.lambdaworks.redis.codec.RedisCodec
import com.persnicketly.net.ServerAddress
import com.persnicketly.Logging

case class Redis(host: String, port: Int) extends Logging {

  def this(address: ServerAddress) = this(address.host, address.port)

  val client = new RedisClient(host, port)

  def using[K, V](codec: RedisCodec[K, V]): RedisWithCodec[K, V] = new RedisWithCodec(codec, this)

}

class RedisWithCodec[K, V](codec: RedisCodec[K, V], redis: Redis) extends Logging {

  val client = redis.client
  val host = redis.host
  val port = redis.port

  def connection: Option[RedisConnection[K, V]] = {
    try { Some(client.connect(codec)) }
    catch {
      case e: Exception => {
        log.warn("Unable to connect to {}", this)
        None
      }
    }
  }

  def exec[T](thunk: RedisConnection[K, V] => T): Option[T] = {
    connection.map({ connection =>
      try { thunk(connection) } finally { connection.close }
    })
  }

}
