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

import utils.HmrcSpec

import play.api.http.Status
import play.api.libs.json.Json
import play.api.test.Helpers._
import play.api.test.{FakeRequest, Helpers}

import uk.gov.hmrc.testppnsmultibox.mocks.{ActionBuildersMockModule, TimeServiceMockModule}
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}

class TimeControllerSpec extends HmrcSpec with ActionBuildersMockModule with TimeServiceMockModule {

  trait Setup {
    val fakeRequest = FakeRequest()

    val underTest = new TimeController(mockTimeService, mockActionBuilders)(Helpers.stubControllerComponents())
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
      val minutes           = 1
      val fakeBoxId         = BoxId("box-id")
      val fakeCorrelationId = CorrelationId.random
      ActionWithBoxId.fetchesBoxId(fakeBoxId)
      NotifyMeIn.returnsCorrelationId(fakeCorrelationId)

      val result = underTest.notifyMeIn(minutes)(fakeRequest)

      status(result) shouldBe Status.ACCEPTED
      contentAsJson(result) shouldBe Json.toJson(NotificationResponse(fakeBoxId, fakeCorrelationId))
    }
  }
}
