package com.persnicketly.queue

import com.persnicketly.readability.model.{Meta,User}

object BookmarkRequestsQueue extends Queue {
  val queueName = "bookmarks-requests";
  def addAll(meta: Meta, user: User): Unit = {
  }
}
