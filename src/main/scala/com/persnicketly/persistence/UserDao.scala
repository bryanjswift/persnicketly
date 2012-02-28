package com.persnicketly.persistence

import com.mongodb.casbah.Imports._
import com.persnicketly.readability.model.User
import org.bson.types.ObjectId

object UserDao extends Dao {
  val collectionName = "users"
  
  val saveTimer = metrics.timer("user-save")
  val usersGauge = metrics.gauge("num-users")(all.size)

  // Initialize indexes
  collection.ensureIndex(MongoDBObject("user_id" -> 1))
  collection.ensureIndex(MongoDBObject("request_token_value" -> 1))

  /**
   * Provide a way to get all the verified users in the DB
   * @return an Iterator of Users
   */
  def all: List[User] =
    collection.distinct("user_id").filter(_ != null).map(i => this.get(i.asInstanceOf[Int]).get).toList

  /**
   * Provide a way to remove a User
   * @param user to delete
   * @return Some(User) if removed, None otherwise
   */
  def delete(user: User): Option[User] = {
    log.warn("Removing {}", user)
    user.id.flatMap(id => collection.findAndRemove(MongoDBObject("_id" -> id)).map(User.apply))
  }

  /**
   * Get a User by object id
   * @param _id - identifier assigned to User object when stored
   * @return Some(User) if found None otherwise
   */
  def get(id: ObjectId): Option[User] =
    collection.findOneByID(id).map(User.apply)

  /**
   * Get a User by request token
   * @param requestToken - token passed back when verifying via oauth
   * @return Some(User) if found None otherwise
   */
  def get(requestToken: String): Option[User] =
    collection.findOne(MongoDBObject("request_token_value" -> requestToken)).map(User.apply)

  /**
   * Get a User by user_id
   * @param userId - unique id provided by Readability API
   * @return Some(User) if found None otherwise
   */
  def get(userId: Int): Option[User] =
    collection.findOne(MongoDBObject("user_id" -> userId)).map(User.apply)

  /**
   * Create an ObjectId for id and send it to UserDao#get(org.bson.types.ObjectId)
   * @param id to convert to ObjectId
   * @return Some(User) if id exists, None otherwise
   */
  def getById(id: String) = if (id.length == 0) { None } else { get(new ObjectId(id)) }

  /**
   * Create an ObjectId for id string if it exists and pass it to UserDao#get(org.bson.types.ObjectId)
   * @param opt to convert to ObjectId if defined
   * @return Some(User) if opt is defined and a User is found for Some(id)
   */
  def getById(opt: Option[String]) = opt.flatMap(id => get(new ObjectId(id)))

  def prune: Unit = {
    collection.find("last_updated" $exists false)
  }

  /**
   * Save user data by updating existing record or inserting new
   * @param user data to save
   * @return User as it now exists in database
   */
  def save(user: User): User = {
    saveTimer.time {
      val query = user.id match {
        case Some(id) => MongoDBObject("_id" -> id)
        case None => MongoDBObject("request_token_value" -> user.requestToken.value)
      }
      collection.update(query, user, upsert = true, multi = false)
      get(user.requestToken.value).get
    }
  }
}
