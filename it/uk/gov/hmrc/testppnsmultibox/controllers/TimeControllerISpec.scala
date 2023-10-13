/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package uk.gov.hmrc.testppnsmultibox.controllers

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.AsyncHmrcSpec

import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.FakeRequest
import play.api.test.Helpers.{status, _}
import uk.gov.hmrc.http.test.WireMockSupport

import uk.gov.hmrc.testppnsmultibox.models.ErrorResponse
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId
import uk.gov.hmrc.testppnsmultibox.stubs.{AuthStub, PushPullNotificationConnectorStub}

class TimeControllerISpec extends AsyncHmrcSpec with WireMockSupport with GuiceOneAppPerSuite with AuthStub with PushPullNotificationConnectorStub {

  val stubConfig = Configuration(
    "microservice.services.auth.port"                        -> wireMockPort,
    "microservice.services.push-pull-notifications-api.port" -> wireMockPort,
    "metrics.enabled"                                        -> false,
    "auditing.enabled"                                       -> false
  )

  override def fakeApplication() = GuiceApplicationBuilder()
    .configure(stubConfig)
    .build()

  trait Setup {
    val underTest = app.injector.instanceOf[TimeController]

    val boxName  = "test/ppns-multibox##1.0##callbackUrl"
    val clientId = "client-id"
    val boxId    = BoxId.random
  }

  "currentTime" should {

    "return 200" in new Setup {
      val result = route(app, FakeRequest("GET", "/current-time")).get

      status(result) mustBe OK
    }
  }

  "notifyMeIn" should {

    "return 202 after checking authorisation and fetching the boxId" in new Setup {
      authoriseStandardApplicationRetrieves(clientId)
      getBoxStubSucceeds(boxName, clientId, boxId)

      val result = route(app, FakeRequest("GET", "/notify-me-in/10").withHeaders(AUTHORIZATION -> "Bearer token")).get

      status(result) mustBe ACCEPTED
    }
  }

  "notifyMeAt" should {

    "return 202 after binding the boxId as a UUID" in new Setup {
      authoriseStandardApplicationRetrieves(clientId)
      validateBoxOwnershipStubReturns(boxId, clientId, valid = true)

      val result = route(app, FakeRequest("GET", s"/notify-me-at/${boxId.value}/in/10").withHeaders(AUTHORIZATION -> "Bearer token")).get

      status(result) mustBe ACCEPTED
    }

    "return 400 when boxId is not a UUID" in new Setup {
      val result = route(app, FakeRequest("GET", s"/notify-me-at/malformed-box-id/in/10").withHeaders(AUTHORIZATION -> "Bearer token")).get

      status(result) mustBe BAD_REQUEST
      contentAsJson(result) mustBe ErrorResponse("BAD_REQUEST", "Box ID is not a UUID").asJson
    }
  }
}
