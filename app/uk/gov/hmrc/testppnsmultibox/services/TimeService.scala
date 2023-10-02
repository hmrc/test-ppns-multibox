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
import java.time.temporal.ChronoUnit.HOURS
import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.FiniteDuration

import akka.actor.ActorSystem

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.connectors.PushPullNotificationConnector
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}
import uk.gov.hmrc.testppnsmultibox.repositories.TimedNotificationRepository

@Singleton()
class TimeService @Inject() (
    uuidService: UuidService,
    timedNotificationRepository: TimedNotificationRepository,
    pushPullNotificationConnector: PushPullNotificationConnector,
    actorSystem: ActorSystem,
    clock: Clock
  )(implicit ec: ExecutionContext
  ) {

  def notifyMeAfter(delay: FiniteDuration, boxId: BoxId)(implicit hc: HeaderCarrier): CorrelationId = {
    val correlationId = uuidService.correlationId
    val notifyAt      = clock.instant.plusMillis(delay.toMillis)
    timedNotificationRepository.insert(TimedNotification(boxId, correlationId, notifyAt, notifyAt.plus(1, HOURS)))

    val job: Runnable = () => {
      pushPullNotificationConnector.postNotifications(boxId, correlationId, s"Notify at $notifyAt")
        .flatMap(notificationId =>
          timedNotificationRepository.complete(boxId, correlationId, notificationId)
        )
    }
    actorSystem.scheduler.scheduleOnce(delay, job)

    correlationId
  }

}
