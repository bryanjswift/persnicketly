package com.persnicketly.readability.api

import dispatch.json.Js._
import dispatch.json.{Js, JsString, Obj}
import dispatch.json.{Extract, JsValue, JsObject}
import com.persnicketly.readability.model.Article

object ArticleExtractor extends Extract[Article] {
  def unapply(js: JsValue) = js match {
    case JsObject(m) => 
      if (ArticleJson.keys.forall(k => m.contains(k))) {
        Some(
          Article(
            ArticleJson.articleId(js),
            ArticleJson.title(js),
            ArticleJson.domain(js),
            ArticleJson.url(js),
            ArticleJson.excerpt.unapply(js),
            ArticleJson.processed(js)
          )
        )
      } else {
        None
      }
    case _ =>
      None
  }
}

object ArticleJson extends Js {
  val title = 'title ? str
  val url = 'url ? str
  val excerpt = 'excerpt ? str
  val domain = 'domain ? str
  val articleId = 'id ? str
  val processed = 'processed ? bool
  val keys = List(JsString('title), JsString('domain), JsString('url), JsString('id))
}