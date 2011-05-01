package com.pernicketly.web

import com.yammer.dropwizard.Service
import com.yammer.dropwizard.service.Jersey

object Persnicketly extends Service with Jersey {
  def name = "web-service"
  
  override def banner = Some("""
This is the Persnicketly web service. And it's starting.
""")
}
