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

package uk.gov.hmrc.testppnsmultibox.mocks

import scala.concurrent.Future.successful

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.testppnsmultibox.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}
import uk.gov.hmrc.testppnsmultibox.repositories.TimedNotificationRepository

trait TimedNotificationRepositoryMockModule extends MockitoSugar with ArgumentMatchersSugar {

  val mockTimedNotificationRepository = mock[TimedNotificationRepository]

  object Insert {

    def returns(timedNotification: TimedNotification) =
      when(mockTimedNotificationRepository.insert(*[TimedNotification])).thenReturn(successful(timedNotification))

    def verifyCalledWith(timedNotification: TimedNotification) =
      verify(mockTimedNotificationRepository).insert(eqTo(timedNotification))
  }

  object Complete {

    def verifyNotCalled() =
      verify(mockTimedNotificationRepository, never).complete(*[BoxId], *[CorrelationId], *[NotificationId])

    def verifyCalledWith(boxId: BoxId, correlationId: CorrelationId, notificationId: NotificationId) =
      verify(mockTimedNotificationRepository).complete(eqTo(boxId), eqTo(correlationId), eqTo(notificationId))
  }

}
