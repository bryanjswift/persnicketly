import sbt._
import assembly._
import maven._

class PersnicketlyWebProject(info: ProjectInfo) extends DefaultProject(info)
                                                        with MavenDependencies
                                                        with IdeaProject
                                                        with AssemblyBuilder {

  // Always check for new versions of snapshot dependencies.
  override def snapshotUpdatePolicy = SnapshotUpdatePolicy.Always

  // Repositories
  val codasRepo = "Coda's Repo" at "http://repo.codahale.com"
  val googleMaven = "Google Maven" at "http://google-maven-repository.googlecode.com/svn/repository/"
  // for simple-velocity
  val bjsRepo = "Bryan J Swift's Repository" at "http://repos.bryanjswift.com/maven2/"
  val sunRepo = "Sun Repo" at "http://download.java.net/maven/2/"
  val yammerRepo = "Yammer's Internal Repo" at "http://repo.yammer.com/maven/"

  // Service Dependencies
  val dropWizard = "com.yammer" %% "dropwizard" % "0.0.3-SNAPSHOT" withSources()
  // Velocity
  val velocity = "org.apache.velocity" % "velocity" % "1.6.4"
  val simpleVelocity = "bryanjswift" %% "simple-velocity" % "0.3.4"

  // Test Dependencies
  val scalatest = "org.scalatest" % "scalatest" % "1.2" % "test->default"

  override def fork = forkRun(List(
    "-server", // make sure we're using the 64-bit server VM
    "-d64",
    "-XX:+UseParNewGC", // use parallel GC for the new generation
    "-XX:+UseConcMarkSweepGC", // use concurrent mark-and-sweep for the old generation
    "-XX:+CMSParallelRemarkEnabled", // use multiple threads for the remark phase
    "-XX:+AggressiveOpts", // use the latest and greatest in JVM tech
    "-XX:+UseFastAccessorMethods", // be sure to inline simple accessor methods
    "-XX:+UseBiasedLocking", // speed up uncontended locks
    "-Xss128k", // reduce the thread stack size, freeing up space for the heap
    "-Xmx500M", // same with the max heap size
    //      "-XX:+PrintGCDetails",                 // log GC details to stdout
    //      "-XX:+PrintGCTimeStamps",
    "-XX:+HeapDumpOnOutOfMemoryError" // dump the heap if we run out of memory
  ))

  lazy val server = runTask(
    getMainClass(true), runClasspath, List("server", "config.json")
  ) dependsOn(compile) describedAs("Runs Example Service with config.json")

  // override looking for jars in ./lib
  override def dependencyPath = sourceDirectoryName / mainDirectoryName / "lib"
  // override path to managed dependency cache
  override def managedDependencyPath = "project" / "lib_managed"
  // java compile options
  override def javaCompileOptions = super.javaCompileOptions ++ List(JavaCompileOption("-Xlint:unchecked"), JavaCompileOption("-g"))
}
