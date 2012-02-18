package com.persnicketly.redis

import com.persnicketly.readability.api.BookmarkRequestConditions
import java.nio.ByteBuffer

class BookmarkRequestConditionsCodec extends StringKeyCodec[BookmarkRequestConditions] {

  def decodeValue(buffer: ByteBuffer): BookmarkRequestConditions = {
    val bytes = new Array[Byte](buffer.remaining)
    buffer.get(bytes)
    BookmarkRequestConditions(bytes)
  }

  def encodeValue(conditions: BookmarkRequestConditions): Array[Byte] = conditions.toByteArray()

}
