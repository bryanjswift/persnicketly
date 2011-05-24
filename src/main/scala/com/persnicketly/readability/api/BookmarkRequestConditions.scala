package com.persnicketly.readability.api

import com.persnicketly.Serializer
import com.persnicketly.readability.model.User
import org.joda.time.DateTime

case class BookmarkRequestConditions(page: Int, count: Int, since: Option[DateTime], user: User) extends Serializer

object BookmarkRequestConditions {
  def apply(bytes: Array[Byte]) = Serializer.apply[BookmarkRequestConditions](bytes)
}
