package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.mongodb.casbah.commons.conversions.scala._
import com.persnicketly.Persnicketly
import com.persnicketly.readability.model.{TokenHelper, User, UserData}
import org.bson.types.ObjectId
import org.joda.time.DateTime

class UserDao extends Dao {
  import UserDao._
  val collectionName = "users"

  /**
   * Provide a way to get all the verified users in the DB
   * @return an Iterator of Users
   */
  def all(): List[User] =
    collection.find("username" $exists true).map(o => dbobject2user(o)).toList

  /**
   * Get a User by object id
   * @param _id - identifier assigned to User object when stored
   * @return Some(User) if found None otherwise
   */
  def get(_id: ObjectId): Option[User] =
    collection.findOneByID(_id).map(o => dbobject2user(o))

  /**
   * Get a User by request token
   * @param requestToken - token passed back when verifying via oauth
   * @return Some(User) if found None otherwise
   */
  def get(requestToken: String): Option[User] =
    collection.findOne(MongoDBObject("request_token_value" -> requestToken)).map(o => dbobject2user(o))

  /**
   * Get a User by user_id
   * @param userId - unique id provided by Readability API
   * @return Some(User) if found None otherwise
   */
  def get(userId: Int): Option[User] =
    collection.findOne(MongoDBObject("user_id" -> userId)).map(o => dbobject2user(o))

  /**
   * Save user data by updating existing record or inserting new
   * @param user data to save
   * @return User as it now exists in database
   */
  def save(user: User): User = {
    val query = user.id match {
      case Some(id) => MongoDBObject("_id" -> id)
      case None => MongoDBObject("request_token_value" -> user.requestToken.value)
    }
    collection.update(query, user, upsert = true, multi = false)
    get(user.requestToken.value).get
  }

  /**
   * Before letting this object get collected make sure the connection is closed
   */
  override def finalize() = {
    super.finalize()
    connection.close
  }
}

object UserDao {
  implicit def user2dbobject(user: User): DBObject = {
    val builder = MongoDBObject.newBuilder
    user.id.foreach(id => builder += ("_id" -> id))
    builder += "request_token_value" -> user.requestToken.value
    builder += "request_token_secret" -> user.requestToken.secret
    user.accessToken.foreach(t => {
      builder += "access_token_value" -> t.value
      builder += "access_token_secret" -> t.secret
    })
    user.verifier.foreach(v => builder += ("verifier" -> v))
    user.personalInfo.foreach(info => {
      builder += "user_id" -> info.userId
      builder += "username" -> info.username
      builder += "first_name" -> info.firstName
      builder += "last_name" -> info.lastName
    })
    user.lastProcessed.foreach(d => builder += ("last_processed" -> d))
    builder += ("last_updated" -> new DateTime)
    builder.result
  }

  implicit def dbobject2user(o: DBObject): User = {
    val rtv = o.getAs[String]("request_token_value")
    val rts = o.getAs[String]("request_token_secret")
    val atv = o.getAs[String]("access_token_value")
    val ats = o.getAs[String]("access_token_secret")
    User(
      o._id,
      TokenHelper(rtv, rts).get,
      TokenHelper(atv, ats),
      o.getAs[String]("verifier"),
      o.getAs[DateTime]("last_processed"),
      UserData(
        o.getAs[Int]("user_id"),
        o.getAs[String]("username"),
        o.getAs[String]("first_name"),
        o.getAs[String]("last_name")
      )
    )
  }

  /**
   * Get a new DAO object
   * @return a new instance of UserDao
   */
  private def dao = { new UserDao }

  /**
   * Proxy to UserDao instance to get data
   * @return Iterator of verified users in DB
   */
  def all() = dao.all()

  /**
   * Proxy to UserDao instance to get data
   * @param id to retrieve
   * @return Some(User) if id exists, None otherwise
   */
  def get(id: ObjectId) = dao.get(id)

  /**
   * Proxy to UserDao instance to get data
   * @param requestToken to retrieve user for
   * @return Some(User) if requestToken exists, None otherwise
   */
  def get(requestToken: String) = dao.get(requestToken)

  /**
   * Proxy to a UserDao object to save data
   * @param user to save
   * @return saved User
   */
  def save(user: User) = { dao.save(user) }
}
