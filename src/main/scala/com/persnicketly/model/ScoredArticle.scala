package com.persnicketly.model

import com.persnicketly.readability.model.Article
import org.bson.types.ObjectId

case class ScoredArticle(id: Option[ObjectId], article: Article, score: Double)
