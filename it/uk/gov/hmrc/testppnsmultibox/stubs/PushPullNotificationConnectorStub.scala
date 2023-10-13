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

package uk.gov.hmrc.testppnsmultibox.stubs

import java.util.UUID

import com.github.tomakehurst.wiremock.client.WireMock._

import play.api.libs.json.Json
import play.api.test.Helpers._

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, NotificationId}

trait PushPullNotificationConnectorStub {

  def getBoxStubSucceeds(boxName: String, clientId: String, boxId: BoxId): Any =
    stubFor(
      get(urlPathEqualTo("/box"))
        .withQueryParam("boxName", equalTo(boxName))
        .withQueryParam("clientId", equalTo(clientId))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.obj(
              "boxId"         -> boxId.value,
              "boxName"       -> boxName,
              "boxCreator"    -> Json.obj("clientId" -> clientId),
              "applicationId" -> UUID.randomUUID().toString,
              "clientManaged" -> false
            ).toString)
        )
    )

  def getBoxStubNotFound(boxName: String, clientId: String): Any =
    stubFor(
      get(urlPathEqualTo("/box"))
        .withQueryParam("boxName", equalTo(boxName))
        .withQueryParam("clientId", equalTo(clientId))
        .willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
            .withBody(Json.obj("code" -> "BOX_NOT_FOUND", "message" -> "Box not found").toString)
        )
    )

  def validateBoxOwnershipStubReturns(boxId: BoxId, clientId: String, valid: Boolean): Any =
    stubFor(
      post(urlPathEqualTo("/cmb/validate"))
        .withHeader(ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
        .withRequestBody(equalToJson(Json.obj("boxId" -> boxId, "clientId" -> clientId).toString))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.obj("valid" -> valid).toString)
        )
    )

  def validateBoxOwnershipStubThrowsException(boxId: BoxId, clientId: String): Any =
    stubFor(
      post(urlPathEqualTo("/cmb/validate"))
        .withRequestBody(equalToJson(Json.obj("boxId" -> boxId.value.toString, "clientId" -> clientId).toString))
        .willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
            .withBody(Json.obj("code" -> NOT_FOUND, "message" -> "Box not found").toString)
        )
    )

  def postNotificationsStubForBoxIdReturns(boxId: BoxId, notificationId: NotificationId): Any =
    stubFor(
      post(urlPathEqualTo(s"/box/${boxId.value}/notifications"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.obj("notificationId" -> notificationId).toString)
        )
    )
}
