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
  override protected def getInjector:Injector = Guice.createInjector(new PersnicketlyServletModule)
}

private class PersnicketlyServletModule extends ServletModule {
  private val log = LoggerFactory.getLogger(classOf[PersnicketlyServletModule])
  override protected def configureServlets() {
    Persnicketly.Config // Read configs on startup
    val jerseyParams = Map(PackagesResourceConfig.PROPERTY_PACKAGES -> "com.persnicketly.web.resource,com.codahale.jersey.providers,com.codahale.jersey.providers,com.codahale.jersey.inject")
    serve("/", "/home", "/thanks").`with`(classOf[HomeServlet])
    serve("/readability/login").`with`(classOf[LoginServlet])
    serve("/readability/sign-out").`with`(classOf[LogoutServlet])
    serve("/readability/callback").`with`(classOf[CallbackServlet])
    serve("/learn-more", "/about").`with`(classOf[TemplateServlet])
    serve("/article/*").`with`(classOf[ArticleServlet])
    serve("/d/*").`with`(classOf[JerseyServletContainer], jerseyParams)
    log.info("** Servlets Ready **")
  }
}

@Singleton
private class JerseyServletContainer extends ServletContainer { }
