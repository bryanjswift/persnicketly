package com.persnicketly.web.health

import com.yammer.metrics.core.HealthCheck
import com.yammer.metrics.core.HealthCheck.Result
import com.google.inject.{Singleton, Inject}

@Singleton
class AliveHealthCheck() extends HealthCheck {
  def name = "alive"

  def check = {
    Result.healthy()
  }
}
