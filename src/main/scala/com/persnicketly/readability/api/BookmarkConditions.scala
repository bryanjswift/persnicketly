package com.persnicketly.readability.api

import com.persnicketly.Serializer
import com.persnicketly.readability.model.User
import org.joda.time.DateTime

case class BookmarksConditions(page: Int, count: Int, since: DateTime, user: User) extends Serializer

object BookmarksConditions {
  def apply(bytes: Array[Byte]) = Serializer.apply[BookmarksConditions](bytes)
}
