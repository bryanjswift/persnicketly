package com.persnicketly.readability.api

import dispatch.json.Js._
import dispatch.json.{Js, JsString}
import dispatch.json.{Extract, JsValue, JsObject}
import com.persnicketly.readability.model.Meta

object MetaJson extends Js {
  val totalPages = 'num_pages ? num
  val page = 'page ? num
  val totalCount = 'item_count_total ? num
  val count = 'item_count ? num
  val keys = List(JsString('num_pages), JsString('page), JsString('item_count_total), JsString('item_count))
}

object MetaExtractor extends Extract[Meta] {
  def unapply(js: JsValue) = js match {
    case JsObject(m) => 
      if (MetaJson.keys.forall(k => m.contains(k))) {
        Some(
          Meta(
            MetaJson.totalPages(js).toInt,
            MetaJson.page(js).toInt,
            MetaJson.totalCount(js).toInt,
            MetaJson.count(js).toInt
          )
        )
      } else {
        None
      }
    case _ =>
      None
  }
}

