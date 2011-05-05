import sbt._
import assembly._
import maven._

class PersnicketlyWebProject(info: ProjectInfo) extends DefaultWebProject(info)
                                                        with MavenDependencies
                                                        with IdeaProject
                                                        with AssemblyBuilder {

  // for Jersey
  val JavaNet2Repository = "Java.net Repository for Maven" at "http://download.java.net/maven/2/"
  // for metrics and fig and jersey-guice-nogrizzly and jersey-scala
  val codasRepo = "Coda's Repo" at "http://repo.codahale.com"
  // for guice
  val googleMaven = "Google Maven" at "http://google-maven-repository.googlecode.com/svn/repository/"
  // for simple-velocity
  val bjsRepo = "Bryan J Swift's Repository" at "http://repos.bryanjswift.com/maven2/"
  // for ??
  val yammerRepo = "Yammer's Internal Repo" at "http://repo.yammer.com/maven/"

  // The many faces of Jetty
  val jettyVersion = "7.4.0.v20110414"
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % jettyVersion
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % jettyVersion
  val jettyServlets = "org.eclipse.jetty" % "jetty-servlets" % jettyVersion
  val servletApi = "javax.servlet" % "servlet-api" % "2.5"
  // guice
  val guice = "com.google.inject" % "guice" % "3.0"
  val guiceServlet = "com.google.inject.extensions" % "guice-servlet" % "3.0"
  // Jersey
  val jerseyScala = "com.codahale" %% "jersey-scala" % "0.1.3"
  // TODO: 05/01/11 <bryanjswift> -- Change back to regular packaging once
  // http://java.net/jira/browse/JERSEY-697 is resolved.
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice-nogrizzly" % "1.6"
  // configuration with JSON files
  val fig = "com.codahale" %% "fig" % "1.1.1"
  // JSON interaction
  val jerkson = "com.codahale" %% "jerkson" % "0.1.7"
  // health checking
  val metrics = "com.yammer" %% "metrics" % "2.0.0-BETA11"
  // SLF4J for a nicer logging interface
  val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1"
  val slf4jJDK = "org.slf4j" % "slf4j-log4j12" % "1.6.1"
  // Velocity
  val velocity = "org.apache.velocity" % "velocity" % "1.6.4"
  val simpleVelocity = "bryanjswift" %% "simple-velocity" % "0.3.4"
  // oauth
  val oauth = "net.databinder" %% "dispatch-oauth" % "0.8.0"
  val nio = "net.databinder" %% "dispatch-nio" % "0.8.0"
  val jsonHttp = "net.databinder" %% "dispatch-http-json" % "0.8.0"
  // Joda Time for nice immutable dates
  val jodaTime = "joda-time" % "joda-time" % "1.6.2"

  // for specs via ScalaTest
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test"

  // override looking for jars in ./lib
  override def dependencyPath = sourceDirectoryName / mainDirectoryName / "lib"
  // override path to managed dependency cache
  override def managedDependencyPath = "project" / "lib_managed"
  // java compile options
  override def javaCompileOptions = super.javaCompileOptions ++ List(JavaCompileOption("-Xlint:unchecked"), JavaCompileOption("-g"))
  // manually define jetty classpath
  override def jettyClasspath = managedDependencyPath / "compile" * "*.jar"
  // don't scan directories - using JRebel
  override def scanDirectories = Nil
}
