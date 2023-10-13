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

package uk.gov.hmrc.testppnsmultibox.ppns.connectors

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.AsyncHmrcSpec

import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}
import uk.gov.hmrc.testppnsmultibox.stubs.PushPullNotificationConnectorStub

class PushPullNotificationConnectorISpec extends AsyncHmrcSpec with WireMockSupport with GuiceOneAppPerSuite with PushPullNotificationConnectorStub {

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
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val underTest = app.injector.instanceOf[PushPullNotificationConnector]

    val boxName        = "box-name"
    val clientId       = "client-id"
    val boxId          = BoxId.random
    val correlationId  = CorrelationId.random
    val notificationId = NotificationId.random
  }

  "getBoxId" should {

    "return a box ID if successful" in new Setup {
      getBoxStubSucceeds(boxName, clientId, boxId)

      val result = await(underTest.getBoxId(boxName, clientId))

      result mustBe Some(boxId)
    }

    "return None if unsuccessful" in new Setup {
      getBoxStubNotFound(boxName, clientId)

      val result = await(underTest.getBoxId(boxName, clientId))

      result mustBe None
    }
  }

  "validateBoxOwnership" should {

    "return true if the box is valid" in new Setup {
      val validity = true
      validateBoxOwnershipStubReturns(boxId, clientId, validity)

      val result = await(underTest.validateBoxOwnership(boxId, clientId))

      result mustBe validity
    }

    "return false if the box is not found" in new Setup {
      validateBoxOwnershipStubThrowsException(boxId, clientId)

      val result = await(underTest.validateBoxOwnership(boxId, clientId))

      result mustBe false
    }
  }

  "postNotifications" should {

    "return a notification ID if successful" in new Setup {
      postNotificationsStubForBoxIdReturns(boxId, notificationId)

      val result = await(underTest.postNotifications(boxId, correlationId, "message"))

      result mustBe notificationId
    }
  }
}
