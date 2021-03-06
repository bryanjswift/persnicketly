package com.persnicketly.redis

import com.lambdaworks.redis.RedisConnection
import com.lambdaworks.redis.codec.RedisCodec
import com.persnicketly.Logging
import scala.util.control.Exception.catching

class RedisWithCodec[K, V](codec: RedisCodec[K, V], redis: Redis) extends Logging {

  val client = redis.client
  val host = redis.host
  val port = redis.port

  private def connection: Option[RedisConnection[K, V]] = {
    try { Some(client.connect(codec)) }
    catch {
      case e: Exception => {
        log.warn("Unable to connect to {}", this)
        None
      }
    }
  }

  def exec[T](thunk: RedisConnection[K, V] => T): Option[T] = {
    connection.flatMap({ conn =>
      catching(classOf[Exception]).either {
        try {
          thunk(conn)
        } finally {
          conn.close
        }
      } match {
        case Left(e) => {
          log.error("Unexpected error occured during processing", e)
          None
        }
        case Right(t) => Some(t)
      }
    })
  }

  def info: RedisInfo = RedisInfo(exec({ connection => connection.info }).getOrElse(""), redis)

  def isAlive: Boolean = exec({ connection => connection.ping == "PONG" }).getOrElse(false)

  override def toString(): String = redis.toString

}
