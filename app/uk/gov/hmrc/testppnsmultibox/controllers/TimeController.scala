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
import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

import play.api.mvc.{Action, AnyContent, ControllerComponents}
import uk.gov.hmrc.auth.core.AuthProvider.StandardApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals._
import uk.gov.hmrc.auth.core.{AuthProviders, AuthorisedFunctions}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendController

import uk.gov.hmrc.testppnsmultibox.connectors.AuthConnector
import uk.gov.hmrc.testppnsmultibox.models.{ErrorResponse, NotificationResponse, TimeResponse}
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId
import uk.gov.hmrc.testppnsmultibox.ppns.services.BoxService
import uk.gov.hmrc.testppnsmultibox.services.TimeService

@Singleton()
class TimeController @Inject() (val authConnector: AuthConnector, boxService: BoxService, timeService: TimeService)(cc: ControllerComponents)(implicit ec: ExecutionContext)
    extends BackendController(cc) with AuthorisedFunctions {

  def currentTime(): Action[AnyContent] = Action.async { _ =>
    Future.successful(Ok(TimeResponse(Instant.now()).asJson))
  }

  def notifyMeIn(seconds: Int): Action[AnyContent] = Action.async { implicit request =>
    authorised(AuthProviders(StandardApplication)).retrieve(clientId) {
      case Some(clientId) =>
        boxService.getBoxId(clientId).map {
          case None        => BadRequest(ErrorResponse("BAD_REQUEST", s"A notification box was not found for client ID $clientId").asJson)
          case Some(boxId) =>
            val correlationId = timeService.notify(boxId, seconds.seconds)
            Accepted(NotificationResponse(boxId, correlationId).asJson)
        }
      case None           => Future.successful(Unauthorized(ErrorResponse("UNAUTHORIZED", "A client ID could not be retrieved after endpoint authorisation").asJson))
    } recover recovery
  }

  def notifyMeAt(boxId: BoxId, seconds: Int): Action[AnyContent] = Action.async { implicit request =>
    authorised(AuthProviders(StandardApplication)).retrieve(clientId) {
      case Some(clientId) =>
        boxService.validateBoxOwnership(boxId, clientId).map {
          case false => BadRequest(ErrorResponse("BAD_REQUEST", s"The provided box is not owned by client ID $clientId").asJson)
          case true  =>
            val correlationId = timeService.notify(boxId, seconds.seconds)
            Accepted(NotificationResponse(boxId, correlationId).asJson)
        }
      case None           => Future.successful(Unauthorized(ErrorResponse("UNAUTHORIZED", "A client ID could not be retrieved after endpoint authorisation").asJson))
    } recover recovery
  }
}
