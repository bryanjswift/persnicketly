package com.persnicketly.web

import org.slf4j.LoggerFactory
import com.google.inject.servlet.{GuiceServletContextListener, ServletModule}
import com.google.inject.{Guice, Injector, Singleton}
import com.sun.jersey.spi.container.servlet.ServletContainer
import com.sun.jersey.api.core.PackagesResourceConfig
import scala.collection.JavaConversions.asJavaMap
import com.persnicketly.web.servlet.RootServlet
import com.persnicketly.web.servlet.readability.{CallbackServlet, LoginServlet}

class PersnicketlyServletConfig extends GuiceServletContextListener {
  private val log = LoggerFactory.getLogger(classOf[PersnicketlyServletConfig])
  override protected def getInjector():Injector = Guice.createInjector(new PersnicketlyServletModule)
}

private class PersnicketlyServletModule extends ServletModule {
  private val log = LoggerFactory.getLogger(classOf[PersnicketlyServletModule])
  override protected def configureServlets():Unit = {
    val jerseyParams = Map(PackagesResourceConfig.PROPERTY_PACKAGES -> "com.persnicketly.web.resource,com.codahale.jersey.providers,com.codahale.jersey.providers,com.codahale.jersey.inject")
    serve("/").`with`(classOf[RootServlet])
    serve("/readability/login").`with`(classOf[LoginServlet])
    serve("/readability/callback").`with`(classOf[CallbackServlet])
    serve("/d/*").`with`(classOf[JerseyServletContainer], jerseyParams)
  }
}

@Singleton
private class JerseyServletContainer extends ServletContainer { }
