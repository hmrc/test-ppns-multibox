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

package uk.gov.hmrc.testppnsmultibox.domain.models

import java.time.Instant
import java.time.temporal.ChronoUnit.HOURS

import play.api.libs.json.Json
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}

case class TimedNotification(boxId: BoxId, correlationId: CorrelationId, notifyAt: Instant, completed: Boolean = false) {
  val expiresAt = notifyAt.plus(1, HOURS)
}

object TimedNotification {

  implicit val ordering: Ordering[TimedNotification] = Ordering.fromLessThan((a, b) => a.notifyAt isBefore b.notifyAt)

  implicit val dateFormat = MongoJavatimeFormats.instantFormat
  implicit val format     = Json.format[TimedNotification]
}
