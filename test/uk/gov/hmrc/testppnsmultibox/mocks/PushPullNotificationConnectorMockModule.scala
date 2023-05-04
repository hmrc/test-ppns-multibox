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

import uk.gov.hmrc.testppnsmultibox.ppns.connectors.PushPullNotificationConnector
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

trait PushPullNotificationConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  val mockPushPullNotificationConnector = mock[PushPullNotificationConnector]

  object GetBoxId {

    def returnsBoxId(boxId: BoxId) =
      when(mockPushPullNotificationConnector.getBoxId(*, *)(*)).thenReturn(successful(Some(boxId)))

    def returnsNoBox() =
      when(mockPushPullNotificationConnector.getBoxId(*, *)(*)).thenReturn(successful(None))

    def verifyCalledAtLeastOnceWith(boxName: String, clientId: String) =
      verify(mockPushPullNotificationConnector, atLeastOnce).getBoxId(eqTo(boxName), eqTo(clientId))(*)
  }

  object PostNotifications {

    def returnsNotificationId(notificationId: NotificationId) = {
      when(mockPushPullNotificationConnector.postNotifications(*[BoxId], *[CorrelationId], *)(*)).thenReturn(successful(notificationId))
    }

    def verifyCalledWith(boxId: BoxId, correlationId: CorrelationId, message: String) =
      verify(mockPushPullNotificationConnector).postNotifications(eqTo(boxId), eqTo(correlationId), eqTo(message))(*)
  }

}
