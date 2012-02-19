import sbt._
import assembly._
import maven._

class PersnicketlyWebProject(info: ProjectInfo) extends DefaultWebProject(info)
                                                        with IdeaProject
                                                        with MavenDependencies
                                                        with AssemblyBuilder {

  // for Jersey
  val JavaNet2Repository = "Java.net Repository for Maven" at "http://download.java.net/maven/2/"
  // for metrics and fig and jersey-guice-nogrizzly and jersey-scala
  val codasRepo = "Coda's Repo" at "http://repo.codahale.com"
  // for guice
  val googleMaven = "Google Maven" at "http://google-maven-repository.googlecode.com/svn/repository/"
  // for simple-velocity
  val bjsRepo = "Bryan J Swift's Repository" at "http://repos.bryanjswift.com/maven2/"

  // The many faces of Jetty
  val jettyVersion = "7.5.4.v20111024"
  val jettyWebapp = "org.eclipse.jetty" % "jetty-webapp" % jettyVersion
  val jettyServer = "org.eclipse.jetty" % "jetty-server" % jettyVersion
  val jettyServlet = "org.eclipse.jetty" % "jetty-servlet" % jettyVersion
  val jettyServlets = "org.eclipse.jetty" % "jetty-servlets" % jettyVersion
  val servletApi = "javax.servlet" % "servlet-api" % "2.5"
  // guice
  val guiceVersion = "3.0"
  val guice = "com.google.inject" % "guice" % guiceVersion
  val guiceServlet = "com.google.inject.extensions" % "guice-servlet" % guiceVersion
  // Jersey
  val jerseyGuice = "com.sun.jersey.contribs" % "jersey-guice" % "1.9.1"
  val jerseyScala = "com.codahale" %% "jersey-scala" % "0.2.0"
  // configuration with JSON files
  val fig = "com.codahale" %% "fig" % "1.1.7"
  // SLF4J for a nicer logging interface
  val slf4j = "org.slf4j" % "slf4j-api" % "1.6.1"
  val slf4jJDK = "org.slf4j" % "slf4j-log4j12" % "1.6.1"
  // Velocity
  val simpleVelocity = "bryanjswift" %% "simple-velocity" % "0.3.5"
  // oauth
  val scribe = "org.scribe" % "scribe" % "1.3.0"
  // Joda Time for nice immutable dates
  val jodaTime = "joda-time" % "joda-time" % "1.6.2"
  // Command line argument processing
  val cli = "commons-cli" % "commons-cli" % "1.2"
  // MongoDB
  val casbahVersion = "2.1.5-1"
  val casbahQuery = "com.mongodb.casbah" %% "casbah-query" % casbahVersion
  val casbahCore = "com.mongodb.casbah" %% "casbah-core" % casbahVersion
  val casbahCommons = "com.mongodb.casbah" %% "casbah-commons" % casbahVersion
  // Redis
  val lettuc = "com.lambdaworks" % "lettuce" % "2.0.0"
  // Metrics
  val metricsScala = "com.yammer.metrics" %% "metrics-scala" % "2.0.0-BETA19"
  val metricsServlet = "com.yammer.metrics" % "metrics-servlet" % "2.0.0-BETA19"

  // for specs via ScalaTest
  val junit = "junit" % "junit" % "4.10" % "test"
  val scalatest = "org.scalatest" %% "scalatest" % "1.6.1" % "test"

  // produce a jar
  override protected def defaultMainArtifact = Artifact(artifactID, "jar", "jar")
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
