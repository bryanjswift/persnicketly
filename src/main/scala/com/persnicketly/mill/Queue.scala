package com.persnicketly.mill

import com.persnicketly.{Logging,Persnicketly}
import com.rabbitmq.client.{Channel,ConnectionFactory,QueueingConsumer}
import java.util.concurrent.atomic.AtomicInteger
import scala.collection.mutable.Set
import scala.util.control.Exception.catching

trait Queue extends Logging {
  type Delivery = QueueingConsumer.Delivery
  def queueName: String
  val args: java.util.Map[String, Object] = null
  val exchange = ""
  def config = Persnicketly.Config("queue." + queueName).as[QueueConfig]
  def processDelivery(delivery: QueueingConsumer.Delivery): Boolean
  private val rejectedDeliveries = Set[Long]()

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
    // result is immediately matched as Either[Exception, T]
    catching(classOf[java.net.ConnectException], classOf[java.io.IOException], classOf[Exception]).either {
      val connection = factory.newConnection()
      val channel = connection.createChannel()
      try {
        channel.queueDeclare(config.name, config.durable, config.exclusive, config.autodelete, args)
        channel.basicQos(config.prefetch)
        thunk(channel)
      } finally {
        channel.close
        connection.close
      }
    } match {
      case Left(e) => {
        e match {
          case ce: java.net.ConnectException => log.error("Unable to connect to AMQP", ce)
          case ioe: java.io.IOException => log.error("Unable to open connection or channel to declare queue", ioe)
          case _ => log.error("Unexpected error occured during processing", e)
        }
        None
      }
      case Right(t) => Some(t)
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
        val result =
          try { processDelivery(delivery) }
          catch { case e: Exception => { log.error("Unable to process {}", delivery, e); false } }

        if (result) {
          counter.incrementAndGet
          channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false)
        } else {
          val tag = delivery.getEnvelope().getDeliveryTag()
          log.warn("Rejected delivery of {}", tag)
          channel.basicNack(tag, false, rejectedDeliveries.contains(tag))
          rejectedDeliveries += tag
        }
      }
      log.warn("Consumer quitting after processing {} deliveries", counter.get)
      counter.get
    }
  }
}
