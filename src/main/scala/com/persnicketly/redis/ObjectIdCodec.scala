package com.persnicketly.redis

import java.nio.ByteBuffer
import org.bson.types.ObjectId

class ObjectIdCodec extends StringKeyCodec[ObjectId] {

  def decodeValue(buffer: ByteBuffer): ObjectId = {
    val bytes = new Array[Byte](buffer.remaining)
    buffer.get(bytes)
    new ObjectId(bytes)
  }

  def encodeValue(id: ObjectId): Array[Byte] = id.toByteArray()

}
