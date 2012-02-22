:load project/test/inClassLoader.scala

import com.persnicketly.readability.Api
import com.persnicketly.persistence._
import com.persnicketly.Persnicketly
import com.persnicketly.readability.api._

val u = inClassLoader(classOf[com.persnicketly.persistence.Connection$]) {
  UserDao.get(new org.bson.types.ObjectId("4dd0bde098c5e77564395ba0")).get
}

