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

package uk.gov.hmrc.testppnsmultibox.ppns

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import cats.data.OptionT

import play.api.mvc.{ActionTransformer, Request, WrappedRequest}
import uk.gov.hmrc.play.bootstrap.backend.controller.BackendHeaderCarrierProvider

import uk.gov.hmrc.testppnsmultibox.config.AppConfig
import uk.gov.hmrc.testppnsmultibox.ppns.connectors.PushPullNotificationConnector
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

case class RequestWithBoxId[A](request: Request[A], boxId: BoxId) extends WrappedRequest[A](request)

@Singleton
class BoxIdTransformer @Inject() (appConfig: AppConfig, pushPullNotificationConnector: PushPullNotificationConnector)(implicit val executionContext: ExecutionContext)
    extends ActionTransformer[Request, RequestWithBoxId]
    with BackendHeaderCarrierProvider {

  override protected def transform[A](request: Request[A]): Future[RequestWithBoxId[A]] =
    (for {
      clientId <- OptionT.fromOption[Future](request.headers.get(CustomHeaders.XClientId))
      boxId    <- OptionT.liftF(pushPullNotificationConnector.getBoxId(s"${appConfig.apiContext}##${appConfig.apiVersion}##callbackUrl", clientId)(hc(request)))
    } yield boxId.getOrElse(
      throw MissingBoxException(s"A box was not found for clientId $clientId")
    ))
      .fold(
        throw MissingClientIdException(s"${CustomHeaders.XClientId} was not provided as a header")
      )(
        RequestWithBoxId(request, _)
      )
}
