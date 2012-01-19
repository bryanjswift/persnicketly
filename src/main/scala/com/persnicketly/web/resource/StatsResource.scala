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
  def stats() = Stats(UserDao.all.size, ArticleDao.count, BookmarkDao.count)

  @GET @Path("/users")
  def userStats() = Stats(UserDao.all.size, -1, -1)

  @GET @Path("/articles")
  def articleStats() = Stats(-1, ArticleDao.count, -1)

  @GET @Path("/bookmarks")
  def bookmarkStats() = Stats(-1, -1, BookmarkDao.count)
}

case class Stats(users: Long, articles: Long, bookmarks: Long)
