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

import java.time.Clock
import java.time.temporal.ChronoUnit.MINUTES
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future, blocking}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.domain.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.connectors.PushPullNotificationConnector
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}
import uk.gov.hmrc.testppnsmultibox.repositories.TimedNotificationRepository

@Singleton()
class TimeService @Inject() (
    uuidService: UuidService,
    timedNotificationRepository: TimedNotificationRepository,
    pushPullNotificationConnector: PushPullNotificationConnector,
    sleepService: SleepService,
    clock: Clock
  )(implicit ec: ExecutionContext
  ) {

  def notifyMeIn(minutes: Int, boxId: BoxId)(implicit hc: HeaderCarrier): CorrelationId = {
    val correlationId = uuidService.correlationId
    val notifyAt      = clock.instant().plus(minutes, MINUTES)
    timedNotificationRepository.insert(TimedNotification(boxId, correlationId, notifyAt))
    Future {
      blocking {
        sleepService.sleepFor(minutes * 60_000)
      }
    }.map { _ =>
      pushPullNotificationConnector.postNotifications(boxId, correlationId, s"Notify at $notifyAt")
        .map { notificationId =>
          timedNotificationRepository.complete(boxId, correlationId, notificationId)
        }
    }
    correlationId
  }

}
