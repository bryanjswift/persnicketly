package com.persnicketly.mill

import com.persnicketly.{Logging,Persnicketly}
import com.rabbitmq.client.{Channel,ConnectionFactory,QueueingConsumer}
import java.util.concurrent.atomic.AtomicInteger

abstract class Queue extends Logging {
  type Delivery = QueueingConsumer.Delivery
  def queueName: String
  val args: java.util.Map[String, Object] = null
  val exchange = ""
  def config = Persnicketly.Config("queue." + queueName).as[QueueConfig]
  def processDelivery(delivery: QueueingConsumer.Delivery): Boolean

  /** Process something within a try/catch/finally with a Channel
    * @param queue configuration used when declaring the channel
    * @param thunk to process with a channel. This is excpected to throw an
    *   Exception if problems occur during processing
    * @return Some(thunk()). None if exception occurs.
    */
  def withChannel[T](queue: QueueConfig)(thunk: Channel => T): Option[T] = {
    val factory = new ConnectionFactory()
    factory.setHost(Persnicketly.Config("queue.host").or("localhost"))
    factory.setPort(Persnicketly.Config("queue.port").or(5672))
    val connection = factory.newConnection()
    val channel = connection.createChannel()
    try {
      channel.queueDeclare(config.name, config.durable, config.exclusive, config.autodelete, args)
      channel.basicQos(config.prefetch)
      Some(thunk(channel))
    } catch {
      case ioe: java.io.IOException => {
        log.error("Unable to open connection or channel to declare queue", ioe)
        None
      }
      case e: Exception => {
        log.error("Error occurred in processing", e)
        None
      }
    } finally {
      channel.close
      connection.close
    }
  }

  /**
   * Start a QueueingConsumer that performs process method for each delivery. If
   * process returns false then the consumer finishes.
   * @return Option containing the number of deliveries processed
   */
  def startConsumer: Option[Int] = {
    withChannel(config) { channel =>
      val consumer = new QueueingConsumer(channel)
      channel.basicConsume(queueName, false, consumer)
      var continue = true
      val counter = new AtomicInteger(0)
      while (continue) {
        val delivery = consumer.nextDelivery
        val result = processDelivery(delivery)
        if (result) {
          counter.incrementAndGet
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false)
        } else {
          continue = false
        }
      }
      log.info("Consumer quitting after processing {} deliveries", counter.get)
      counter.get
    }
  }
}
