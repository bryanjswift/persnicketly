package com.persnicketly

trait Command extends Logging {
  def group: ThreadGroup
  def start: List[Thread]

  def async(name: String, work: => Unit): Thread = {
    log.info("Starting thread '{}'", name)
    val t = new Thread(group, name) { override def run() = work }
    t.start
    t
  }
}
