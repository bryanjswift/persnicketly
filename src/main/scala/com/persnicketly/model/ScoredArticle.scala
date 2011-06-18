package com.persnicketly.model

import com.persnicketly.readability.model.Article
import org.bson.types.ObjectId

case class ScoredArticle(id: Option[ObjectId],
                         article: Article,
                         favoriteCount: Double,
                         count: Double) extends Ordered[ScoredArticle] {

  val numFavorites = favoriteCount.toInt
  val numBookmarks = count.toInt
  val score = favoriteCount + count

  def compare(that: ScoredArticle): Int = {
    if (that.favoriteCount != this.favoriteCount) { that.favoriteCount.compareTo(this.favoriteCount) }
    else if (that.count != this.count) { that.count.compareTo(this.count) }
    else { that.article.articleId.compareTo(this.article.articleId) }
  }
}
