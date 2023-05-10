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
import javax.inject.{Inject, Named, Singleton}

import akka.actor.ActorRef

import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.actors.NotifyAtActor.NotifyAt
import uk.gov.hmrc.testppnsmultibox.domain.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}
import uk.gov.hmrc.testppnsmultibox.repositories.TimedNotificationRepository

@Singleton()
class TimeService @Inject() (
    uuidService: UuidService,
    timedNotificationRepository: TimedNotificationRepository,
    @Named("notify-at-actor") notifyAtActor: ActorRef,
    clock: Clock
  ) {

  def notifyMeIn(minutes: Int, boxId: BoxId)(implicit hc: HeaderCarrier): CorrelationId = {
    val correlationId = uuidService.correlationId
    val notifyAt      = clock.instant().plus(minutes, MINUTES)
    timedNotificationRepository.insert(TimedNotification(boxId, correlationId, notifyAt))
    notifyAtActor ! NotifyAt(notifyAt, boxId, correlationId, hc)
    correlationId
  }

}
