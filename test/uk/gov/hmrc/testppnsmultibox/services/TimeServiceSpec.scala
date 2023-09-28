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

import java.time.temporal.ChronoUnit.HOURS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.DurationInt

import akka.actor.ActorSystem
import org.scalatest.BeforeAndAfterAll
import utils.{FixedClock, HmrcSpec}

import play.api.test.{DefaultAwaitTimeout, FutureAwaits}
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.mocks.{PushPullNotificationConnectorMockModule, TimedNotificationRepositoryMockModule, UuidServiceMockModule}
import uk.gov.hmrc.testppnsmultibox.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, NotificationId}

class TimeServiceSpec extends HmrcSpec with UuidServiceMockModule with TimedNotificationRepositoryMockModule with PushPullNotificationConnectorMockModule
    with FixedClock with BeforeAndAfterAll with FutureAwaits with DefaultAwaitTimeout {

  val system = ActorSystem("TimeServiceSpec")

  override def afterAll(): Unit = await(system.terminate())

  trait Setup {
    implicit val hc = HeaderCarrier()
    val underTest   = new TimeService(mockUuidService, mockTimedNotificationRepository, mockPushPullNotificationConnector, system, clock)
  }

  "notifyMeIn" should {
    "return a correlation ID and perform an asynchronous action" in new Setup {
      CorrelationIdGenerator.returnsFakeCorrelationId

      val boxId             = BoxId.random
      val delay             = 3.seconds
      val notifyAt          = instant.plusMillis(delay.toMillis)
      val timedNotification = TimedNotification(boxId, fakeCorrelationId, notifyAt, notifyAt.plus(1, HOURS))
      Insert.returns(timedNotification)

      val notificationId = NotificationId.random
      PostNotifications.returns(notificationId)

      val correlationId = underTest.notifyMeAfter(delay, boxId)

      correlationId shouldBe fakeCorrelationId
      Insert.verifyCalledWith(timedNotification)

      Thread.sleep(delay.toMillis - 1_000)
      Complete.verifyNotCalled()
      PostNotifications.verifyNotCalled()

      // Verify that scheduler performs its operation
      Thread.sleep(2_000)
      PostNotifications.verifyCalledWith(boxId, correlationId, s"Notify at $notifyAt")
      Complete.verifyCalledWith(boxId, correlationId, notificationId)
    }
  }
}
