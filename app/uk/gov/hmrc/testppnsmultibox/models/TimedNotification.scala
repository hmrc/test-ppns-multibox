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

package uk.gov.hmrc.testppnsmultibox.models

import java.time.Instant

import play.api.libs.json.{Format, Json, OFormat}
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

case class TimedNotification(
    boxId: BoxId,
    correlationId: CorrelationId,
    notifyAt: Instant,
    expiresAt: Instant,
    completed: Boolean = false,
    notificationId: Option[NotificationId] = None
  )

object TimedNotification {

  implicit val ordering: Ordering[TimedNotification] = Ordering.fromLessThan((a, b) => a.notifyAt isBefore b.notifyAt)

  implicit val dateFormat: Format[Instant]        = MongoJavatimeFormats.instantFormat
  implicit val format: OFormat[TimedNotification] = Json.format[TimedNotification]
}
