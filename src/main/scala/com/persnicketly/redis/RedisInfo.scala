package com.persnicketly.redis

import scala.util.matching.Regex

case class RedisInfo(info: String, redis: Redis) {
  val host = redis.host
  val masterHost = extract("""master_host:(.*)""".r)
  val masterPort = extract("""master_port:(.*)""".r, default = "-1").toInt
  val port = redis.port
  val role = extract("""role:(.*)""".r)

  private def extract(regex: Regex, default: String = "unknown"): String =
    regex.findFirstMatchIn(info).map(m => m.group(1)).getOrElse(default)
}
