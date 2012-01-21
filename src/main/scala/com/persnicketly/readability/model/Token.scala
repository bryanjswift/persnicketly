package com.persnicketly.readability.model

import org.scribe.model.{Token => ScribeToken}

case class Token(value: String, secret: String)

object Token {
  implicit def apply(scribeToken: ScribeToken): Token = Token(scribeToken.getToken, scribeToken.getSecret)
  implicit def token2scribetoken(token: Token): ScribeToken = new ScribeToken(token.value, token.secret)
}
