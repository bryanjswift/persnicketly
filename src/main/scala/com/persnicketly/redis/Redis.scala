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

