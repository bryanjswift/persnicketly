package com.persnicketly.mill

import com.persnicketly.{Logging, Persnicketly, Serializer}
import com.persnicketly.Persnicketly.Config
import com.redis.RedisClient
import com.redis.serialization.Parse
import com.yammer.metrics.scala.Instrumented
import scala.util.control.Exception.catching

trait RedisQueue[T <: { def toByteArray(): Array[Byte] }] extends Logging with Instrumented {

  type Delivery = T

  def queueName: String

  def parser: Parse[Delivery]

  def process(delivery: Delivery): Boolean

  implicit val p = parser

  def publish[K](delivery: Delivery): Option[Delivery] = {
    withClient {
      client => client.lpush(queueName, delivery.toByteArray).map(_ => delivery)
    } flatMap(o => o)
  }

  def timeout = Config("redis.timeout").or(60)

  def withClient[K](thunk: RedisClient => K): Option[K] = {
    catching(classOf[Exception]).either {
      val client = new RedisClient(Config("redis.host").or("localhost"), Config("redis.port").or(6379))
      try {
        thunk(client)
      } finally {
        client.disconnect
      }
    } match {
      case Left(e) => {
        log.error("Unexpected error occured during processing", e)
        None
      }
      case Right(t) => Some(t)
    }
  }

  def startConsumer: Option[Long] = {
    withClient { client =>
      log.debug("client -- {}", client)
      while (client != null) {
        val delivery = 
          client.brpop[String, Delivery](timeout, queueName) match {
            case None => None
            case Some((key, delivery)) => Some(delivery)
          }
        try {
          delivery.map(process)
        } catch {
          case e: Exception =>
            log.error("Unable to process {}", delivery, e)
        }
      }
      log.warn("Consumer quitting")
      client.llen(queueName).get.toLong
    }
  }
}
