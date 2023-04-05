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

package uk.gov.hmrc.testppnsmultibox.controllers

import java.time.Instant
import javax.inject.{Inject, Singleton}
import scala.concurrent.Future

import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.testppnsmultibox.ppns.ActionBuilders
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}
import uk.gov.hmrc.testppnsmultibox.services.TimeService

case class TimeResponse(message: Instant)

object TimeResponse {
  implicit val format = Json.format[TimeResponse]
}

case class NotificationResponse(boxId: BoxId, correlationId: CorrelationId)

object NotificationResponse {
  implicit val format = Json.format[NotificationResponse]
}

@Singleton()
class TimeController @Inject() (timeService: TimeService, actionBuilders: ActionBuilders)(cc: ControllerComponents)
    extends BackendController(cc) {

  import actionBuilders._

  def currentTime(): Action[AnyContent] = Action.async { _ =>
    Future.successful(Ok(Json.toJson(TimeResponse(Instant.now()))))
  }

  def notifyMeIn(minutes: Int): Action[AnyContent] = actionWithBoxId.async { implicit requestWithBoxId =>
    val boxId         = requestWithBoxId.boxId
    val correlationId = timeService.notifyMeIn(minutes, boxId)
    Future.successful(Accepted(Json.toJson(NotificationResponse(boxId, correlationId))))
  }
}
