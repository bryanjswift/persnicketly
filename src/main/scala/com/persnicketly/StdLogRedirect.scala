package com.persnicketly

import java.io.{PrintStream, OutputStream, ByteArrayOutputStream}
import org.slf4j.{Logger, LoggerFactory}
import com.persnicketly.web.Persnicketly

object StdLogRedirect {
  def redirectLogs = {
    val stdout = System.out
    System.setOut(proxy(stdout, "stdout", (s: String) => { stdout.println(s) }))
    stdout.println("Redirecting stdout to slf4j")
  }

  private def proxy(real: PrintStream, name: String, r: (String) => Unit): PrintStream = {
    new LoggingPrintStream(real, new LoggingOutputStream(LoggerFactory.getLogger(name)), r)
  }
}

class LoggingPrintStream(real: PrintStream, os: OutputStream, r: (String) => Unit) extends PrintStream(os, true) {

  override def print(s: String) = {
    val re = Persnicketly.Config("log.re").or("""INF: \[console logger\] """).r
    if (re.findFirstIn(s).isDefined) {
      super.print(re.replaceFirstIn(s, ""))
    } else {
      r(s)
    }
  }
}

class LoggingOutputStream(private val logger: Logger) extends ByteArrayOutputStream { 
 
    private val lineSeparator = System.getProperty("line.separator")
 
    override def flush(): Unit = {
      this.synchronized {
        super.flush(); 
        val record = this.toString(); 
        super.reset(); 

        if (record.length() == 0 || record.equals(lineSeparator)) { 
          // avoid empty records 
          return; 
        } 

        logger.info(record); 
      }
    } 
} 

