package com.persnicketly.readability.api

import com.persnicketly.Persnicketly
import org.scribe.builder.api.DefaultApi10a
import org.scribe.builder.ServiceBuilder
import org.scribe.oauth.OAuthService
import org.scribe.model.Token

class ReadabilityApi extends DefaultApi10a {
  val svc = "https://www.readability.com/api/rest/v1/oauth/"

  override def getAccessTokenEndpoint = svc + "access_token"

  override def getRequestTokenEndpoint = svc + "request_token"

  override def getAuthorizationUrl(requestToken: Token) = svc + "authorize?oauth_token=" + requestToken.getToken
}

object ReadabilityApi {
  import com.persnicketly.Persnicketly.Config

  private val key = Config("oauth.readability.key").or("")
  private val secret = Config("oauth.readability.secret").or("")
  private val callback = String.format("http://%s/readability/callback", Config("http.domain").or("persnicketly.com"))

  def service: OAuthService = new ServiceBuilder().provider(classOf[ReadabilityApi])
      .callback(callback)
      .apiKey(key)
      .apiSecret(secret)
      .build()

}
