package com.persnicketly.redis

import com.persnicketly.readability.model.Article
import java.nio.ByteBuffer

class ArticleCodec extends StringKeyCodec[Article] {

  def decodeValue(buffer: ByteBuffer): Article = {
    val bytes = new Array[Byte](buffer.remaining)
    buffer.get(bytes)
    Article(bytes)
  }

  def encodeValue(article: Article): Array[Byte] = article.toByteArray()

}
