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

import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

import utils.HmrcSpec

import play.api.http.Status
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.mocks.{AuthConnectorMockModule, BoxServiceMockModule, TimeServiceMockModule}
import uk.gov.hmrc.testppnsmultibox.models.{ErrorResponse, NotificationResponse, TimeResponse}
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}

class TimeControllerSpec extends HmrcSpec with AuthConnectorMockModule with BoxServiceMockModule with TimeServiceMockModule {

  trait Setup {
    implicit val hc = HeaderCarrier()
    val fakeRequest = FakeRequest()

    val underTest = new TimeController(mockAuthConnector, mockBoxService, mockTimeService)(Helpers.stubControllerComponents())

    val fakeBoxId = BoxId.random
    val seconds   = 1
    val clientId  = UUID.randomUUID().toString
  }

  "currentTime" should {
    "return 200 and a TimeResponse body with a time zone of Z" in new Setup {
      val result = underTest.currentTime()(fakeRequest)

      status(result) shouldBe Status.OK
      val body = contentAsJson(result).as[TimeResponse]
      body.message.toString should endWith("Z")
    }
  }

  "notifyMeIn" should {
    "return 202 and a NotificationResponse body" in new Setup {
      val fakeCorrelationId = CorrelationId.random
      Authorise.asStandardApplication(clientId)
      GetBoxId.returns(fakeBoxId)
      Notify.returns(fakeCorrelationId)

      val result = underTest.notifyMeIn(seconds)(fakeRequest)

      status(result) shouldBe Status.ACCEPTED
      contentAsJson(result) shouldBe NotificationResponse(fakeBoxId, fakeCorrelationId).asJson

      GetBoxId.verifyCalledWith(clientId)
    }

    "return 401 if not authorised by a standard app" in new Setup {
      Authorise.asUnsupportedAuthProvider()

      val result = underTest.notifyMeIn(seconds)(fakeRequest)

      status(result) shouldBe Status.UNAUTHORIZED
      contentAsJson(result) shouldBe ErrorResponse("UNAUTHORIZED", "Only standard applications may call this endpoint").asJson
    }

    "return 401 if a client ID cannot be retrieved" in new Setup {
      Authorise.withoutClientId()

      val result = underTest.notifyMeIn(seconds)(fakeRequest)

      status(result) shouldBe Status.UNAUTHORIZED
      contentAsJson(result) shouldBe ErrorResponse("UNAUTHORIZED", "A client ID could not be retrieved after endpoint authorisation").asJson
    }

    "return 400 if a box ID cannot fetched" in new Setup {
      Authorise.asStandardApplication(clientId)
      GetBoxId.returnsNoBox()

      val result = underTest.notifyMeIn(seconds)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe ErrorResponse("BAD_REQUEST", s"A notification box was not found for client ID $clientId").asJson
    }

    "return 500 if there is an unexpected error" in new Setup {
      Authorise.withInternalError()

      val result = underTest.notifyMeIn(seconds)(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred: Internal error").asJson
    }
  }

  "notifyMeAt" should {
    "return 202 and a NotificationResponse body" in new Setup {
      val fakeCorrelationId = CorrelationId.random
      Authorise.asStandardApplication(clientId)
      ValidateBoxOwnership.returns(true)
      Notify.returns(fakeCorrelationId)

      val result = underTest.notifyMeAt(fakeBoxId, seconds)(fakeRequest)

      status(result) shouldBe Status.ACCEPTED
      contentAsJson(result) shouldBe NotificationResponse(fakeBoxId, fakeCorrelationId).asJson

      ValidateBoxOwnership.verifyCalledWith(fakeBoxId, clientId)
    }

    "return 401 if not authorised by a standard app" in new Setup {
      Authorise.asUnsupportedAuthProvider()

      val result = underTest.notifyMeAt(fakeBoxId, seconds)(fakeRequest)

      status(result) shouldBe Status.UNAUTHORIZED
      contentAsJson(result) shouldBe ErrorResponse("UNAUTHORIZED", "Only standard applications may call this endpoint").asJson
    }

    "return 401 if a client ID cannot be retrieved" in new Setup {
      Authorise.withoutClientId()

      val result = underTest.notifyMeAt(fakeBoxId, seconds)(fakeRequest)

      status(result) shouldBe Status.UNAUTHORIZED
      contentAsJson(result) shouldBe ErrorResponse("UNAUTHORIZED", "A client ID could not be retrieved after endpoint authorisation").asJson
    }

    "return 400 if the box ID is not valid" in new Setup {
      Authorise.asStandardApplication(clientId)
      ValidateBoxOwnership.returns(false)

      val result = underTest.notifyMeAt(fakeBoxId, seconds)(fakeRequest)

      status(result) shouldBe Status.BAD_REQUEST
      contentAsJson(result) shouldBe ErrorResponse("BAD_REQUEST", s"The provided box is not owned by client ID $clientId").asJson
    }

    "return 500 if there is an unexpected error" in new Setup {
      Authorise.withInternalError()

      val result = underTest.notifyMeAt(fakeBoxId, seconds)(fakeRequest)

      status(result) shouldBe Status.INTERNAL_SERVER_ERROR
      contentAsJson(result) shouldBe ErrorResponse("INTERNAL_SERVER_ERROR", "An unexpected error occurred: Internal error").asJson
    }
  }
}
