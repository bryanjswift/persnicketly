package com.persnicketly.web

import org.slf4j.LoggerFactory
import com.google.inject.servlet.{GuiceServletContextListener, ServletModule}
import com.google.inject.{Guice, Injector, Singleton}
import com.sun.jersey.spi.container.servlet.ServletContainer
import com.sun.jersey.api.core.PackagesResourceConfig
import scala.collection.JavaConversions.asJavaMap
import com.persnicketly.Persnicketly
import com.persnicketly.web.servlet.{HomeServlet, ArticleServlet, TemplateServlet}
import com.persnicketly.web.servlet.readability.{CallbackServlet, LoginServlet, LogoutServlet}

class PersnicketlyServletConfig extends GuiceServletContextListener {
  private val log = LoggerFactory.getLogger(classOf[PersnicketlyServletConfig])
  override protected def getInjector():Injector = Guice.createInjector(new PersnicketlyServletModule)
}

private class PersnicketlyServletModule extends ServletModule {
  private val log = LoggerFactory.getLogger(classOf[PersnicketlyServletModule])
  override protected def configureServlets(): Unit = {
    val conf = Persnicketly.Config // Really just to read configs on startup
    val jerseyParams = Map(PackagesResourceConfig.PROPERTY_PACKAGES -> "com.persnicketly.web.resource,com.codahale.jersey.providers,com.codahale.jersey.providers,com.codahale.jersey.inject")
    serve("/", "/home").`with`(classOf[HomeServlet])
    serve("/readability/login").`with`(classOf[LoginServlet])
    serve("/readability/sign-out").`with`(classOf[LogoutServlet])
    serve("/readability/callback").`with`(classOf[CallbackServlet])
    serve("/learn-more").`with`(classOf[TemplateServlet])
    serve("/article/list").`with`(classOf[ArticleServlet])
    serve("/d/*").`with`(classOf[JerseyServletContainer], jerseyParams)
  }
}

@Singleton
private class JerseyServletContainer extends ServletContainer { }
