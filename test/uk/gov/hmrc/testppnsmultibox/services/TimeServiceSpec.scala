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

import akka.actor.ActorSystem
import akka.testkit.TestKitBase
import org.scalatest.BeforeAndAfterAll
import utils.{FixedClock, HmrcSpec}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.actors.NotifyAtActor.NotifyAt
import uk.gov.hmrc.testppnsmultibox.domain.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.mocks.{TimedNotificationRepositoryMockModule, UuidServiceMockModule}
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

class TimeServiceSpec extends HmrcSpec with TestKitBase with UuidServiceMockModule with TimedNotificationRepositoryMockModule
    with FixedClock with BeforeAndAfterAll {

  implicit lazy val system = ActorSystem("TimeServiceSpec")

  override protected def testActorName: String = "notify-at-actor"

  override def afterAll(): Unit = shutdown(system)

  trait Setup {
    implicit val hc = HeaderCarrier()

    val underTest = new TimeService(mockUuidService, mockTimedNotificationRepository, testActor, clock)
  }

  "notifyMeIn" should {
    "return a correlation ID and perform an asynchronous action" in new Setup {
      CorrelationIdGenerator.returnsFakeCorrelationId

      val boxId             = BoxId.random
      val minutes           = 1
      val notifyAt          = instant.plus(minutes, MINUTES)
      val timedNotification = TimedNotification(boxId, fakeCorrelationId, notifyAt)
      Insert.returns(timedNotification)

      val correlationId = underTest.notifyMeIn(minutes, boxId)

      correlationId shouldBe fakeCorrelationId
      Insert.verifyCalledWith(timedNotification)
      expectMsg(NotifyAt(notifyAt, boxId, correlationId, hc))
    }
  }
}
