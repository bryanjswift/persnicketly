package com.persnicketly.web.resource

import javax.ws.rs.core.MediaType
import com.google.inject.Singleton
import javax.ws.rs.{GET, Produces, Path}
import com.persnicketly.persistence._

@Path("/stats")
@Produces(Array(MediaType.APPLICATION_JSON))
@Singleton
class StatsResource() {

  @GET @Path("/users")
  def userStats() = {
    val users = UserDao.all
    val unique = users.map(_.personalInfo.get.username).toSet
    UserStats(unique.size, users.size)
  }

  @GET @Path("/articles")
  def articleStats() = {
    ArticleStats(ScoredArticleDao.all.size)
  }
}

case class UserStats(unique: Int, total: Int)
case class ArticleStats(articles: Int)
