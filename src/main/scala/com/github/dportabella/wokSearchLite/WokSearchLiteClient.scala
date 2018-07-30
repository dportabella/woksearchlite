package com.github.dportabella.wokSearchLite

import scala.concurrent._
import scala.concurrent.duration._

class WokSearchLiteClient {
  def authenticate() {
    println("authenticate")
    val authenticateFutureResponse: Future[AuthenticateResponse] = authenticateService.authenticate()
    val authenticateResponse: AuthenticateResponse = Await.result(authenticateFutureResponse, 5 seconds)
    println(s"authenticateResponse: $authenticateResponse")
    val authenticationCookie = authenticateResponse.returnValue.get
    println(s"authenticationCookie: $authenticationCookie")
    authenticationCookieHeader = Some("Cookie" -> s"""SID="$authenticationCookie"""")
  }

  def closeSession() {
    println("closeSession")
    val closeSessionFutureResponse: Future[CloseSessionResponse] = authenticateService.closeSession()
    Await.result(closeSessionFutureResponse, 5.seconds)
    authenticationCookieHeader = None
    println("session closed")
  }

  private var authenticationCookieHeader: Option[(String, String)] = None

  private trait CookieDispatchHttpClientsAsync extends scalaxb.DispatchHttpClientsAsync {
    override lazy val httpClient: CookieDispatchHttpClient = new CookieDispatchHttpClient {}

    trait CookieDispatchHttpClient extends DispatchHttpClient {
      override def request(in: String, address: java.net.URI, headers: Map[String, String]): concurrent.Future[String] = {
        val headers_ = authenticationCookieHeader.map(c => headers + c).getOrElse(headers)
        super.request(in, address, headers_)
      }
    }
  }

  private val authenticateService: WOKMWSAuthenticate =
    new WOKMWSAuthenticateServiceSoapBindings with
      scalaxb.Soap11ClientsAsync with
      CookieDispatchHttpClientsAsync {}
      .service

  val service: WokSearchLite =
    new WokSearchLiteServiceSoapBindings with
      scalaxb.Soap11ClientsAsync with
      CookieDispatchHttpClientsAsync {}
      .service
}
