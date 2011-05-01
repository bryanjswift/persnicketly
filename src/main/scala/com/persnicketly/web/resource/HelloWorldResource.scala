package com.persnicketly.web.resources

import javax.ws.rs.core.MediaType
import com.google.inject.{Inject, Singleton}
import javax.ws.rs.{QueryParam, GET, Produces, Path}
import java.util.concurrent.atomic.AtomicLong
import com.yammer.metrics.guice.Timed
import com.persnicketly.web.data.Saying

@Path("/hello-world")
@Produces(Array(MediaType.APPLICATION_JSON))
@Singleton
class HelloWorldResource() {
  private val counter = new AtomicLong(0)

  @GET
  @Timed(name = "say-hello")
  def sayHello(@QueryParam("name") name: Option[String]) = {
    Saying(counter.incrementAndGet, name.getOrElse("Stranger!"))
  }
}
