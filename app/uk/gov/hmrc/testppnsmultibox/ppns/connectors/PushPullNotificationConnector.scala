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

package uk.gov.hmrc.testppnsmultibox.ppns.connectors

import java.util.UUID
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

object PushPullNotificationConnector {
  case class Config(baseUrl: String)
}

case class GetBoxResponse(boxId: UUID)

object GetBoxResponse {
  implicit val format: OFormat[GetBoxResponse] = Json.format[GetBoxResponse]
}

case class PostNotificationsRequest(correlationId: UUID, message: String)

object PostNotificationsRequest {
  implicit val format: OFormat[PostNotificationsRequest] = Json.format[PostNotificationsRequest]
}

case class PostNotificationsResponse(notificationId: UUID)

object PostNotificationsResponse {
  implicit val format: OFormat[PostNotificationsResponse] = Json.format[PostNotificationsResponse]
}

@Singleton
class PushPullNotificationConnector @Inject() (config: PushPullNotificationConnector.Config, http: HttpClient)(implicit executionContext: ExecutionContext) {

  def getBoxId(boxName: String, clientId: String)(implicit hc: HeaderCarrier): Future[Option[BoxId]] = {
    http.GET[GetBoxResponse](s"${config.baseUrl}/box", Seq("boxName" -> boxName, "clientId" -> clientId))
      .map(x => Some(BoxId(x.boxId)))
      .recover { case _ => None }
  }

  def postNotifications(boxId: BoxId, correlationId: CorrelationId, message: String)(implicit hc: HeaderCarrier): Future[NotificationId] = {
    val body = PostNotificationsRequest(correlationId.value, message)
    http.POST[PostNotificationsRequest, PostNotificationsResponse](s"${config.baseUrl}/box/${boxId.value}/notifications", body)
      .map(x => NotificationId(x.notificationId))
  }
}
