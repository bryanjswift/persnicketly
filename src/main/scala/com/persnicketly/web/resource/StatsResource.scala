package com.persnicketly.web.resource

import javax.ws.rs.core.MediaType
import com.google.inject.Singleton
import javax.ws.rs.{GET, Produces, Path}
import com.persnicketly.persistence._

@Path("/stats")
@Produces(Array(MediaType.APPLICATION_JSON))
@Singleton
class StatsResource() {

  @GET
  def stats() = {
    val bookmarks = new BookmarkDao
    Stats(UserDao.all.size, ScoredArticleDao.all.size, bookmarks.collection.count)
  }

  @GET @Path("/users")
  def userStats() = {
    val users = UserDao.all
    Stats(users.size, -1, -1)
  }

  @GET @Path("/articles")
  def articleStats() = {
    Stats(-1, ScoredArticleDao.all.size, -1)
  }

  @GET @Path("/bookmarks")
  def bookmarkStats() = {
    val bookmarks = new BookmarkDao
    Stats(-1, -1, bookmarks.collection.count)
  }
}

case class Stats(users: Int, articles: Int, bookmarks: Long)
