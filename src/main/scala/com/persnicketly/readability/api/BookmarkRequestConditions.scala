package com.persnicketly.readability.api

import com.persnicketly.Serializer
import com.persnicketly.readability.Api
import com.persnicketly.readability.model.User
import org.joda.time.DateTime

case class BookmarkRequestConditions(
      page: Option[Int],
      count: Option[Int], since: Option[DateTime],
      user: User) extends Serializer {

  val map = Map(
    "page" -> page.getOrElse(1).toString,
    "per_page" -> count.getOrElse(20).toString,
    "updated_since" -> since.getOrElse(new DateTime(0)).toString(Api.datePattern)
  )
}

object BookmarkRequestConditions {
  def apply(bytes: Array[Byte]) = Serializer.apply[BookmarkRequestConditions](bytes)
  def apply(page: Int, count: Int, since: Option[DateTime], user: User) = {
    new BookmarkRequestConditions(Some(page), Some(count), since, user)
  }
}
