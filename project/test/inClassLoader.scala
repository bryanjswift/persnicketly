def inClassLoader[T](cls: Class[_])(f: => T): T = {
  val prev = Thread.currentThread.getContextClassLoader
  try {
    Thread.currentThread.setContextClassLoader(
       cls.getClassLoader
    )
    f
  } finally {
    Thread.currentThread.setContextClassLoader(prev)
  }
}

def icl[T](f: => T): T = inClassLoader(classOf[com.persnicketly.persistence.Connection$])(f)
