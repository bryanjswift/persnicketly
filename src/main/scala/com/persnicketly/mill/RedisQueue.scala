package com.persnicketly.mill

import com.lambdaworks.redis.RedisConnection
import com.persnicketly.{Logging, Parse, Persnicketly, Serializer}
import com.persnicketly.Persnicketly.Config
import com.persnicketly.redis.{RedisCluster, StringKeyCodec}
import com.yammer.metrics.scala.Instrumented
import org.joda.time.DateTime

trait RedisQueue[T] extends Logging with Instrumented {

  /** Type of data being processed by this RedisQueue */
  type Delivery = T

  /** Name of queue in broker */
  def queueName: String

  /** RedisCodec to handle transformation from Array[Byte] to model object */
  def codec: StringKeyCodec[Delivery]

  /** Name of acknowledgement queue in broker */
  def queueAck = queueName + "-ack"

  /** Track queue size */
  def gauge = metrics.gauge(queueName)(size)

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
      client.lrem(queueAck, 1, delivery)
    }
  }

  /**
   * Inform broker delivery was handled unsuccessfully and must be requeued
   * @param delivery to requeue
   */
  def nack(delivery: Delivery): Unit = {
    withClient { client =>
      val transaction = client.multi()
      client.lpush(queueName, delivery)
      client.lrem(queueAck, 1, delivery)
      client.exec()
    }
  }

  /**
   * Add data to be processed later
   * @param delivery to be processed later
   * @return Some(delivery) if successfully added to queue, None otherwise
   */
  def publish(delivery: Delivery): Option[Delivery] = {
    withClient { client =>
      val count = client.lpush(queueName, delivery)
      if (count == 0) { throw new Exception("Failed to push delivery") }
      else { delivery }
    }
  }

  def size: java.lang.Long =
    try { withClient({ c => c.llen(queueName) }).getOrElse(0L) }
    catch {
      case e: NullPointerException => -1L
    }

  /**
   * Start a consumer process for this queue
   * @return Option wrapping number of deliveries remaining in queue when quitting
   */
  def startConsumer: Option[Long] = {
    RedisCluster.using(codec).exec { client =>
      while (true) {
        val value = client.brpoplpush(timeout, queueName, queueAck)
        val delivery = 
          if (value != null) { Some(value) }
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
  def timeout = Config("redis.timeout").or(60L)

  /**
   * Do some activity within the context of a Redis connection
   * @param thunk operation to perform with client
   * @return Option wrapping result of thunk(client) if successful, None if there was an error
   */
  private def withClient[K](thunk: RedisConnection[String, Delivery] => K): Option[K] = {
    RedisCluster.using(codec).exec(thunk)
  }

}
