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

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import play.api.Logging
import play.api.http.HeaderNames.ACCEPT
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.http.HttpReads.Implicits._
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient}

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

object PushPullNotificationConnector {
  case class Config(baseUrl: String)
}

case class GetBoxResponse(boxId: BoxId)

object GetBoxResponse {
  implicit val format: OFormat[GetBoxResponse] = Json.format[GetBoxResponse]
}

case class ValidateBoxOwnershipRequest(boxId: BoxId, clientId: String)

object ValidateBoxOwnershipRequest {
  implicit val format: OFormat[ValidateBoxOwnershipRequest] = Json.format[ValidateBoxOwnershipRequest]
}

case class ValidateBoxOwnershipResponse(valid: Boolean)

object ValidateBoxOwnershipResponse {
  implicit val format: OFormat[ValidateBoxOwnershipResponse] = Json.format[ValidateBoxOwnershipResponse]
}

case class PostNotificationsRequest(correlationId: CorrelationId, message: String)

object PostNotificationsRequest {
  implicit val format: OFormat[PostNotificationsRequest] = Json.format[PostNotificationsRequest]
}

case class PostNotificationsResponse(notificationId: NotificationId)

object PostNotificationsResponse {
  implicit val format: OFormat[PostNotificationsResponse] = Json.format[PostNotificationsResponse]
}

@Singleton
class PushPullNotificationConnector @Inject() (config: PushPullNotificationConnector.Config, http: HttpClient)(implicit executionContext: ExecutionContext)
    extends Logging {

  def getBoxId(boxName: String, clientId: String)(implicit hc: HeaderCarrier): Future[Option[BoxId]] =
    http.GET[GetBoxResponse](
      url = s"${config.baseUrl}/box",
      queryParams = Seq("boxName" -> boxName, "clientId" -> clientId)
    )
      .map(x => Some(x.boxId))
      .recover {
        case e =>
          logger.error(s"Error while getting box ID: ${e.getMessage}")
          None
      }

  def validateBoxOwnership(boxId: BoxId, clientId: String)(implicit hc: HeaderCarrier): Future[Boolean] =
    http.POST[ValidateBoxOwnershipRequest, ValidateBoxOwnershipResponse](
      url = s"${config.baseUrl}/cmb/validate",
      body = ValidateBoxOwnershipRequest(boxId, clientId),
      headers = Seq(ACCEPT -> "application/vnd.hmrc.1.0+json")
    )
      .map(_.valid)
      .recover {
        case e =>
          logger.error(s"Error while validating box ID: ${e.getMessage}")
          false
      }

  def postNotifications(boxId: BoxId, correlationId: CorrelationId, message: String)(implicit hc: HeaderCarrier): Future[NotificationId] =
    http.POST[PostNotificationsRequest, PostNotificationsResponse](
      url = s"${config.baseUrl}/box/${boxId.value}/notifications",
      body = PostNotificationsRequest(correlationId, message)
    )
      .map(_.notificationId)

}
