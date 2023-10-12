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

import com.github.tomakehurst.wiremock.client.WireMock._
import play.api.libs.json.Json
import play.api.test.Helpers._

import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

trait PushPullNotificationConnectorStub {

  val boxName = "box-name"
  val clientId = "client-id"
  val boxId = BoxId.random

  def getBoxStubReturns(response: String): Any =
    stubFor(
      get(urlPathEqualTo("/box"))
        .withQueryParam("boxName", equalTo(boxName))
        .withQueryParam("clientId", equalTo(clientId))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(response)
        )
    )

  def validateBoxOwnershipStubReturns(response: String): Any =
    stubFor(
      post(urlPathEqualTo("/cmb/validate"))
        .withHeader(ACCEPT, equalTo("application/vnd.hmrc.1.0+json"))
        .withRequestBody(equalToJson(Json.obj("boxId" -> boxId.value.toString, "clientId" -> clientId).toString))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(response)
        )
    )

  def validateBoxOwnershipStubThrowsException(): Any =
    stubFor(
      post(urlPathEqualTo("/cmb/validate"))
        .withRequestBody(equalToJson(Json.obj("boxId" -> boxId.value.toString, "clientId" -> clientId).toString))
        .willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
            .withBody(Json.obj("code" -> NOT_FOUND, "message" -> "Box not found").toString)
        )
    )

  def postNotificationsStubForBoxIdReturns(boxId: String)(notificationId: String): Any =
    stubFor(
      post(urlPathEqualTo(s"/box/$boxId/notifications"))
        .willReturn(
          aResponse()
            .withStatus(OK)
            .withBody(Json.obj("notificationId" -> notificationId).toString)
        )
    )
}
