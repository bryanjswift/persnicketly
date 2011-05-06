package com.persnicketly.readability.model

import org.scalatest.WordSpec
import org.scalatest.matchers.ShouldMatchers
import dispatch.json.{JsValue, JsObject}
import dispatch.json.Js._

class BookmarkExtractorSpec extends WordSpec with ShouldMatchers {
  val userId = 7567890
  val bookmarkId = 479280
  val singleJson = """{
                        "user_id": """ + userId + """,
                        "read_percent": "0.00",
                        "date_updated": "2011-05-01 14:23:07",
                        "favorite": false,
                        "article": {
                            "domain": "www.hyperorg.com",
                            "title": "Joho the Blog » A big question",
                            "url": "http://www.hyperorg.com/blogger/2011/05/01/a-big-question/",
                            "excerpt": "Why did the world shatter at the touch of a hyperlink?\nNewspapers, encyclopedias, record companies, telephones, politics, education, analytics, scientifics, genetics, libraries, mass media,…",
                            "word_count": 362,
                            "processed": true,
                            "id": "6sig62k0" 
                        },
                        "id": """ + bookmarkId + """,
                        "date_archived": "2011-05-01 14:23:07",
                        "date_opened": null,
                        "date_added": "2011-05-01 14:23:07",
                        "article_href": "/api/rest/v1/articles/6sig62k0",
                        "date_favorited": null,
                        "archive": true 
                      }"""

  "BookmarkExtractor" should {
    "create Seq[Bookmark] from Seq[JsObject]" in {
      val stream = getClass.getClassLoader.getResourceAsStream("bookmarks.json")
      stream should not be (null)
      val json = JsValue.fromStream(stream).asInstanceOf[JsObject]
      val jsObjects = ('bookmarks ! (list ! obj))(json)
      jsObjects.size should be > (0)
      val bookmarks = jsObjects map BookmarkExtractor
      bookmarks should have size (jsObjects.size)
    }
    "produce bookmarks with values from JsObject" in {
      val js = JsValue.fromString(singleJson).asInstanceOf[JsObject]
      val bookmark = BookmarkExtractor(js)
      bookmark.userId should be (userId)
      bookmark.bookmarkId should be (bookmarkId)
    }
  }
}
