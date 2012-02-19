package com.persnicketly.redis

import com.lambdaworks.redis.codec.{RedisCodec, Utf8StringCodec}
import java.nio.ByteBuffer

trait StringKeyCodec[V] extends RedisCodec[String, V] {

  private val utf = new Utf8StringCodec()

  def decodeKey(bytes: ByteBuffer): String = utf.decodeKey(bytes)

  def encodeKey(key: String): Array[Byte] = utf.encodeKey(key)

}
