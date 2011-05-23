package com.persnicketly.queue

import com.persnicketly.{Logging,Persnicketly}
import com.rabbitmq.client.{Channel,ConnectionFactory}

abstract class Queue extends Logging {
  def queueName: String
  val args: java.util.Map[String, Object] = null
  val exchange = ""
  def config = Persnicketly.Config("queue." + queueName).as[QueueConfig]

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
}
