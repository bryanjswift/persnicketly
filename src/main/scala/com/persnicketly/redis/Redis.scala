package com.persnicketly.redis

import com.lambdaworks.redis.{RedisClient, RedisConnection}
import com.lambdaworks.redis.codec.{RedisCodec, Utf8StringCodec}
import com.persnicketly.net.ServerAddress
import com.persnicketly.Logging

case class Redis(host: String, port: Int) extends Logging {

  def this(address: ServerAddress) = this(address.host, address.port)

  val client = new RedisClient(host, port)

  private val utf = new Utf8StringCodec()

  def isAlive: Boolean = using(utf).isAlive

  def using[K, V](codec: RedisCodec[K, V]): RedisWithCodec[K, V] = new RedisWithCodec(codec, this)

}

