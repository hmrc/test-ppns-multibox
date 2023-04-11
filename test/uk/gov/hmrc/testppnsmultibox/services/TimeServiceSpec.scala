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

package uk.gov.hmrc.testppnsmultibox.services

import java.time.temporal.ChronoUnit.MINUTES

import utils.{FixedClock, HmrcSpec}

import uk.gov.hmrc.testppnsmultibox.domain.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.mocks.{TimedNotificationRepositoryMockModule, UuidServiceMockModule}
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

class TimeServiceSpec extends HmrcSpec with FixedClock with UuidServiceMockModule with TimedNotificationRepositoryMockModule {

  trait Setup {
    val underTest = new TimeService(mockUuidService, mockTimedNotificationRepository, clock)
  }

  "notifyMeIn" should {
    "return a correlation ID" in new Setup {
      val boxId             = BoxId("box-id")
      val minutes           = 1
      val timedNotification = TimedNotification(boxId, fakeCorrelationId, instant.plus(minutes, MINUTES))
      CorrelationIdGenerator.returnsFakeCorrelationId

      val result = underTest.notifyMeIn(minutes, boxId)

      Insert.verifyCalledWith(timedNotification)
      // TODO: Verify that future is created
      result shouldBe fakeCorrelationId
    }
  }
}
