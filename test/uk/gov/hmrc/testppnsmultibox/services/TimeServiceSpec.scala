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
import scala.concurrent.ExecutionContext.Implicits.global

import utils.{FixedClock, HmrcSpec}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.domain.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.mocks.{PushPullNotificationConnectorMockModule, SleepServiceMockModule, TimedNotificationRepositoryMockModule, UuidServiceMockModule}
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, NotificationId}

class TimeServiceSpec extends HmrcSpec with FixedClock with UuidServiceMockModule with TimedNotificationRepositoryMockModule with PushPullNotificationConnectorMockModule
    with SleepServiceMockModule {

  trait Setup {
    implicit val hc = HeaderCarrier()
    val underTest   = new TimeService(mockUuidService, mockTimedNotificationRepository, mockPushPullNotificationConnector, mockSleepService, clock)
  }

  "notifyMeIn" should {
    "return a correlation ID" in new Setup {
      CorrelationIdGenerator.returnsFakeCorrelationId

      val boxId             = BoxId.random
      val minutes           = 1
      val notifyAt          = instant.plus(minutes, MINUTES)
      val timedNotification = TimedNotification(boxId, fakeCorrelationId, notifyAt)
      Insert.returns(timedNotification)

      val notificationId = NotificationId.random
      PostNotifications.returnsNotificationId(notificationId)

      val result = underTest.notifyMeIn(minutes, boxId)

      Insert.verifyCalledWith(timedNotification)
      result shouldBe fakeCorrelationId

      // Verify that blocking Future performs its operation
      Thread.sleep(500)
      SleepFor.verifyCalledWith(minutes * 60_000)
      PostNotifications.verifyCalledWith(boxId, fakeCorrelationId, s"Notify at $notifyAt")
      Complete.verifyCalledWith(boxId, fakeCorrelationId, notificationId)
    }
  }
}
