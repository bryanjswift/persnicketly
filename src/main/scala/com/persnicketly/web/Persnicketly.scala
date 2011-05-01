package com.persnicketly.web

import com.yammer.dropwizard.Service
import com.yammer.dropwizard.service.Jersey
import com.persnicketly.web.health.AliveHealthCheck

object Persnicketly extends Service with Jersey {
  healthCheck[AliveHealthCheck]

  def name = "persnicketly-web"
  
  override def banner = Some("""
This is the Persnicketly web service. And it's starting.
""")
}
