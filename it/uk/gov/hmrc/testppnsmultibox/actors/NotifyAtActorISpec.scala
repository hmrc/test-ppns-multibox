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

package uk.gov.hmrc.testppnsmultibox.actors

import java.time.temporal.ChronoUnit.SECONDS
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.ActorSystem
import akka.testkit.TestKitBase

import org.scalatest.BeforeAndAfterAll
import utils.{FixedClock, HmrcSpec}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.actors.NotifyAtActor.NotifyAt
import uk.gov.hmrc.testppnsmultibox.mocks.{PushPullNotificationConnectorMockModule, TimedNotificationRepositoryMockModule}
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

class NotifyAtActorISpec extends HmrcSpec with TestKitBase with TimedNotificationRepositoryMockModule with PushPullNotificationConnectorMockModule
    with FixedClock with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem("NotifyAtActorSpec")

  override def afterAll(): Unit = shutdown(system)

  trait Setup {
    implicit val hc = HeaderCarrier()

    val underTest = system.actorOf(NotifyAtActor.props(mockTimedNotificationRepository, mockPushPullNotificationConnector, clock))
  }

  "notifyAt" should {
    "perform its asynchronous task" in new Setup {

      val notifyAt      = instant.plus(3, SECONDS)
      val boxId         = BoxId.random
      val correlationId = CorrelationId.random

      val notificationId = NotificationId.random
      PostNotifications.returnsNotificationId(notificationId)

      underTest ! NotifyAt(notifyAt, boxId, correlationId, hc)

      Thread.sleep(2_000)
      Complete.verifyNotCalled()
      PostNotifications.verifyNotCalled()

      // Verify that scheduler performs its operation
      Thread.sleep(2_000)
      PostNotifications.verifyCalledWith(boxId, correlationId, s"Notify at $notifyAt")
      Complete.verifyCalledWith(boxId, correlationId, notificationId)
    }
  }
}
