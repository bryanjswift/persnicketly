package com.persnicketly.model

import com.persnicketly.readability.model.Article
import org.bson.types.ObjectId

case class ScoredArticle(id: Option[ObjectId],
                         article: Article,
                         favoriteCount: Double,
                         count: Double,
                         isBookmarked: Boolean,
                         isFavorited: Boolean) extends Ordered[ScoredArticle] {

  val numFavorites = favoriteCount.toInt
  val numBookmarks = count.toInt
  val score = numFavorites + numBookmarks

  def compare(that: ScoredArticle): Int = {
    if (that.numFavorites != this.numFavorites) { that.numFavorites.compareTo(this.numFavorites) }
    else if (that.numBookmarks != this.numBookmarks) { that.numBookmarks.compareTo(this.numBookmarks) }
    else { that.article.articleId.compareTo(this.article.articleId) }
  }
}

object ScoredArticle {
  def apply(id: Option[ObjectId], article: Article, favoriteCount: Double, count: Double): ScoredArticle =
    new ScoredArticle(id, article, favoriteCount, count, false, false)
}
