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

import java.time.{Clock, Duration, Instant}
import javax.inject.Inject
import scala.concurrent.ExecutionContext

import akka.actor.{Actor, Props}

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.ppns.connectors.PushPullNotificationConnector
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}
import uk.gov.hmrc.testppnsmultibox.repositories.TimedNotificationRepository

class NotifyAtActor @Inject() (
    timedNotificationRepository: TimedNotificationRepository,
    pushPullNotificationConnector: PushPullNotificationConnector,
    clock: Clock
  )(implicit ec: ExecutionContext
  ) extends Actor {

  import NotifyAtActor._

  def receive = {
    case NotifyAt(instant: Instant, boxId: BoxId, correlationId: CorrelationId, hc: HeaderCarrier) =>
      val duration = Duration.between(clock.instant(), instant)

      val job: Runnable = () => {
        pushPullNotificationConnector.postNotifications(boxId, correlationId, s"Notify at $instant")(hc)
          .map { notificationId =>
            timedNotificationRepository.complete(boxId, correlationId, notificationId)
          }
      }

      context.system.scheduler.scheduleOnce(duration, job)
  }
}

object NotifyAtActor {

  def props(
      timedNotificationRepository: TimedNotificationRepository,
      pushPullNotificationConnector: PushPullNotificationConnector,
      clock: Clock
    )(implicit ec: ExecutionContext
    ): Props = Props(new NotifyAtActor(timedNotificationRepository, pushPullNotificationConnector, clock))

  case class NotifyAt(instant: Instant, boxId: BoxId, correlationId: CorrelationId, hc: HeaderCarrier)
}
