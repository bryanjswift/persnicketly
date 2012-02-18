package com.persnicketly.mill

import com.persnicketly.{Logging, Parse, Persnicketly, Serializer}
import com.persnicketly.Persnicketly.Config
import com.yammer.metrics.scala.Instrumented
import org.joda.time.DateTime
import redis.clients.jedis.Jedis
import scala.util.control.Exception.catching

trait RedisQueue[T <: { def toByteArray(): Array[Byte] }] extends Logging with Instrumented {

  /** Type of data being processed by this RedisQueue */
  type Delivery = T

  /** Shortcut to Parse helper */
  val Parse = com.persnicketly.Parse

  /** Name of queue in broker */
  def queueName: String

  /** Name of acknowledgement queue in broker */
  def queueAck = queueName + "-ack"

  /** Parse instance for Delivery type */
  def parser: Parse[Delivery]

  /**
   * Process the data in delivery and return if successful
   * @param delivery to be processed
   * @return true if delivery was processed successfully, false otherwise
   */
  def process(delivery: Delivery): Boolean

  /**
   * Acknowledge delivery has been handled
   * @param delivery which was successfully handled
   */
  def ack(delivery: Delivery): Unit = {
    withClient { client =>
      client.lrem(queueAck.getBytes, 1, delivery.toByteArray())
    }
  }

  /**
   * Inform broker delivery was handled unsuccessfully and must be requeued
   * @param delivery to requeue
   */
  def nack(delivery: Delivery): Unit = {
    withClient { client =>
      val transaction = client.multi()
      transaction.lpush(queueName.getBytes, delivery.toByteArray())
      transaction.lrem(queueAck.getBytes, 1, delivery.toByteArray())
      transaction.exec()
    }
  }

  /**
   * Add data to be processed later
   * @param delivery to be processed later
   * @return Some(delivery) if successfully added to queue, None otherwise
   */
  def publish(delivery: Delivery): Option[Delivery] = {
    withClient { client =>
      val count = client.lpush(queueName.getBytes, delivery.toByteArray())
      if (count == 0) { throw new Exception("Failed to push delivery") }
      else { delivery }
    }
  }

  /**
   * Start a consumer process for this queue
   * @return Option wrapping number of deliveries remaining in queue when quitting
   */
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

  /** How long to wait for next delivery */
  def timeout = Config("redis.timeout").or(60)

  /**
   * Do some activity with the context of a Jedis instance
   * @param thunk operation to perform with client
   * @return Option wrapping result of thunk(client) if successful, None if there was an error
   */
  private def withClient[K](thunk: Jedis => K): Option[K] = {
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
