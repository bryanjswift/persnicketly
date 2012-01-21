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
  import Persnicketly.{oauthCallback => callback,oauthConsumer => consumer}

  def service: OAuthService = new ServiceBuilder().provider(classOf[ReadabilityApi])
      .callback(callback)
      .apiKey(consumer.key)
      .apiSecret(consumer.secret)
      .build()

}
