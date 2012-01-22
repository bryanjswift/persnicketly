package com.persnicketly.readability

import api.ReadabilityApi
import model.Token
import org.scribe.model.Verifier

object Auth {
  private val svc = "https://www.readability.com/api/rest/v1/oauth/"

  def requestToken: Token = ReadabilityApi.service.getRequestToken

  def authorizeUrl(token: Token): String = ReadabilityApi.service.getAuthorizationUrl(token)

  def accessToken(token: Token, verifier: String): Token = {
    val v = new Verifier(verifier)
    ReadabilityApi.service.getAccessToken(token, v)
  }
}

