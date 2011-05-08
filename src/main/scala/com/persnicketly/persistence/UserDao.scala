package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.web.Persnicketly
import com.persnicketly.readability.model.{TokenHelper, User, UserData}
import org.bson.types.ObjectId

class UserDao {
  import UserDao._
  RegisterJodaTimeConversionHelpers()
  val config = Persnicketly.Config
  val connection = MongoConnection(config("db.host").or("localhost"), config("db.port").or(27017))
  val users = connection(config("db.name").or("persnicketly_test"))("users")

  /**
   * Save user data by updating existing record or inserting new
   * @param user data to save
   * @return User as it now exists in database
   */
  def save(user: User): Option[User] = {
    val query = user.id match {
      case Some(id) => MongoDBObject("_id" -> id)
      case None => MongoDBObject("request_token_value" -> user.requestToken.value)
    }
    users.update(query, user, upsert = true, multi = false)
    get(user.requestToken.value)
  }

  /**
   * Get a User by object id
   * @param userId - identifier assigned to User object when stored
   * @return Some(User) if found None otherwise
   */
  def get(userId: ObjectId): Option[User] =
    users.findOneByID(userId).map(o => dbobject2user(o))

  /**
   * Get a User by request token
   * @param requestToken - token passed back when verifying via oauth
   * @return Some(User) if found None otherwise
   */
  def get(requestToken: String): Option[User] =
    users.findOne(MongoDBObject("request_token_value" -> requestToken)).map(o => dbobject2user(o))
}

object UserDao {
  implicit def user2dbobject(user: User): DBObject = {
    val builder = MongoDBObject.newBuilder
    user.id.foreach(id => builder += ("_id" -> id))
    builder += "request_token_value" -> user.requestToken.value
    builder += "request_token_secret" -> user.requestToken.secret
    user.accessToken.foreach(t => {
      builder += "access_token_value" -> user.accessToken.get.value
      builder += "access_token_secret" -> user.accessToken.get.secret
    })
    user.verifier.foreach(v => builder += ("verifier" -> v))
    builder.result
  }
  implicit def dbobject2user(o: DBObject): User = {
    val rtv = o.getAs[String]("request_token_value")
    val rts = o.getAs[String]("request_token_secret")
    val atv = o.getAs[String]("access_token_value")
    val ats = o.getAs[String]("access_token_secret")
    val v = o.getAs[String]("verifier")
    val ud = UserData(o.getAs[String]("username"), o.getAs[String]("first_name"), o.getAs[String]("last_name"))
    User(o._id, TokenHelper(rtv, rts).get, TokenHelper(atv, ats), v, ud)
  }
  def dao = { new UserDao }
  def save(user: User) = { dao.save(user) }
  def get(id: ObjectId) = dao.get(id)
  def get(requestToken: String) = dao.get(requestToken)
}

