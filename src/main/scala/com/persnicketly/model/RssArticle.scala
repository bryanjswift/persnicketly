package com.persnicketly.model

import com.mongodb.casbah.Imports._
import org.joda.time.DateTime

case class RssArticle(id: Option[String], rank: Option[Int], lastRank: Option[Int], updated: Option[DateTime])

object RssArticle {
  implicit def apply(o: DBObject): RssArticle = {
    RssArticle(
      o.getAs[String]("_id"),
      o.getAs[Int]("r"),
      o.getAs[Int]("lr"),
      o.getAs[DateTime]("u")
    )
  }

  implicit def rssarticle2dbobject(rss: RssArticle): DBObject = {
    val builder = MongoDBObject.newBuilder
    rss.id.foreach(id => builder += ("_id" -> id))
    rss.rank.foreach(r => builder += ("r" -> r))
    rss.lastRank.foreach(lr => builder += ("lr" -> lr))
    rss.updated.foreach(u => builder += ("u" -> u))
    builder.result
  }
}

