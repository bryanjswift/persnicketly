package com.persnicketly.readability

import dispatch.nio.Http
import dispatch.oauth.{Consumer, Token}
import dispatch.oauth.OAuth.Request2RequestSigner
import dispatch.Request._

object Auth {

  private val svc = "https://www.readability.com/api/rest/v1/oauth/"

  def request_token(consumer: Consumer, callback_url: String) = {
    val http = new Log4jHttp
    val response = http(svc.secure.POST / "request_token" <@ (consumer, callback_url) as_token)()
    http.shutdown
    response
  }

  def authorize_url(token: Token) = svc / "authorize" with_token token

  def access_token(consumer: Consumer, token: Token, verifier: String) = {
    val http = new Log4jHttp
    val response = http(svc.secure.POST / "access_token" <@ (consumer, token, verifier) >% { m => Token(m).get })()
    http.shutdown
    response
  }
}

