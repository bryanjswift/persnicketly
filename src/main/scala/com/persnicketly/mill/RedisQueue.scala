package com.persnicketly.mill

import com.persnicketly.{Logging, Parse, Persnicketly, Serializer}
import com.persnicketly.Persnicketly.Config
import com.yammer.metrics.scala.Instrumented
import org.joda.time.DateTime
import redis.clients.jedis.Jedis
import scala.util.control.Exception.catching

trait RedisQueue[T <: { def toByteArray(): Array[Byte] }] extends Logging with Instrumented {

  type Delivery = T

  val Parse = com.persnicketly.Parse

  def queueName: String

  def parser: Parse[Delivery]

  def process(delivery: Delivery): Boolean

  def queueAck = queueName + "-ack"

  def ack(delivery: Delivery): Unit = {
    withClient { client =>
      client.lrem(queueAck.getBytes, 1, delivery.toByteArray())
    }
  }

  def nack(delivery: Delivery): Unit = {
    withClient { client =>
      val transaction = client.multi()
      transaction.lpush(queueName.getBytes, delivery.toByteArray())
      transaction.lrem(queueAck.getBytes, 1, delivery.toByteArray())
      transaction.exec()
    }
  }

  def publish(delivery: Delivery): Option[Delivery] = {
    withClient { client =>
      val count = client.lpush(queueName.getBytes, delivery.toByteArray())
      if (count == 0) { throw new Exception("Failed to push delivery") }
      else { delivery }
    }
  }

  def startConsumer: Option[Long] = {
    withClient { client =>
      while (client != null) {
        val bytes = client.brpoplpush(queueName.getBytes, queueAck.getBytes, timeout)
        val delivery = 
          if (bytes != null) { Some(parser(bytes)) }
          else { None }
        try {
          delivery.map(data => {
            val result = process(data) // should trigger an 'in threshold nack'
            ack(data)
            result
          })
        } catch {
          case e: Exception => {
            log.error("Unable to process {}", delivery, e)
            delivery.map(data => { nack(data) })
          }
        }
      }
      log.warn("Consumer quitting")
      client.llen(queueName)
    }
  }

  def timeout = Config("redis.timeout").or(60)

  def withClient[K](thunk: Jedis => K): Option[K] = {
    catching(classOf[Exception]).either {
      val client = new Jedis(Config("redis.host").or("localhost"), Config("redis.port").or(6379))
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

}
