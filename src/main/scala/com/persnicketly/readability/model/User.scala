package com.persnicketly.readability.model

import org.bson.types.ObjectId
import dispatch.oauth.Token

case class User(_id: Option[ObjectId], requestToken: Token, accessToken: Option[Token], verifier: Option[String])

