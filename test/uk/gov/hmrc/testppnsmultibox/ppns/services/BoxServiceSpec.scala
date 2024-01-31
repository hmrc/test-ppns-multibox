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

package uk.gov.hmrc.testppnsmultibox.ppns.services

import java.util.UUID

import utils.HmrcSpec

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.config.AppConfig
import uk.gov.hmrc.testppnsmultibox.mocks.PushPullNotificationConnectorMockModule
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

class BoxServiceSpec extends HmrcSpec with PushPullNotificationConnectorMockModule with FutureAwaits with DefaultAwaitTimeout {

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val mockAppConfig = mock[AppConfig]
    when(mockAppConfig.apiContext).thenReturn("test/ppns-multibox")
    when(mockAppConfig.apiVersion).thenReturn("1.0")

    val underTest = new BoxService(mockAppConfig, mockPushPullNotificationConnector)

    val boxName  = "test/ppns-multibox##1.0##callbackUrl"
    val clientId = UUID.randomUUID().toString
  }

  "getBoxId" should {
    "return a box ID if the box is found" in new Setup {
      val boxId = BoxId.random
      GetBoxId.returns(boxId)

      val maybeBox = await(underTest.getBoxId(clientId))

      maybeBox shouldBe Some(boxId)
      GetBoxId.verifyCalledWith(boxName, clientId)
    }

    "return None if the box is not found" in new Setup {
      GetBoxId.returnsNoBox()

      val maybeBox = await(underTest.getBoxId(clientId))

      maybeBox shouldBe None
      GetBoxId.verifyCalledWith(boxName, clientId)
    }
  }

  "validateBoxOwnership" should {
    "return true if the box is valid" in new Setup {
      val boxId = BoxId.random
      ValidateBoxOwnership.returns(true)

      val result = await(underTest.validateBoxOwnership(boxId, clientId))

      result shouldBe true
      ValidateBoxOwnership.verifyCalledWith(boxId, clientId)
    }
  }
}
