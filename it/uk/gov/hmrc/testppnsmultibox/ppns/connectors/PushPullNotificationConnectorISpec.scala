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

import com.github.tomakehurst.wiremock.client.WireMock._

import org.scalatest.matchers.must.Matchers.convertToAnyMustWrapper
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import utils.AsyncHmrcSpec

import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.mvc.Http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport

import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

class PushPullNotificationConnectorISpec extends AsyncHmrcSpec with WireMockSupport with GuiceOneAppPerSuite {

  val stubConfig = Configuration(
    "microservice.services.auth.port"                        -> wireMockPort,
    "microservice.services.push-pull-notifications-api.port" -> wireMockPort,
    "metrics.enabled"                                        -> false,
    "auditing.enabled"                                       -> false
  )

  override def fakeApplication() = GuiceApplicationBuilder()
    .configure(stubConfig)
    .build()

  trait Setup {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val underTest = app.injector.instanceOf[PushPullNotificationConnector]

    val boxName        = "box-name"
    val clientId       = "client-id"
    val boxId          = BoxId.random
    val correlationId  = CorrelationId.random
    val notificationId = NotificationId.random

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

  "getBoxId" should {

    "return a box ID if successful" in new Setup {
      getBoxStubReturns(
        s"""{
           |    "boxId": "${boxId.value}",
           |    "boxName": "$boxName",
           |    "boxCreator": {
           |        "clientId": "$clientId"
           |    },
           |    "applicationId": "f255573c-445f-43cc-86f3-0d76122ed7b5",
           |    "clientManaged": false
           |}""".stripMargin.stripLineEnd
      )

      val result = await(underTest.getBoxId(boxName, clientId))

      result mustBe Some(boxId)
    }

    "return None if unsuccessful" in new Setup {
      getBoxStubReturns(
        """{
          |    "code": "BOX_NOT_FOUND",
          |    "message": "Box not found"
          |}""".stripMargin.stripLineEnd
      )

      val result = await(underTest.getBoxId(boxName, clientId))

      result mustBe None
    }
  }

  "postNotifications" should {

    "return a notification ID if successful" in new Setup {
      postNotificationsStubForBoxIdReturns(boxId.value.toString)(notificationId.value.toString)

      val result = await(underTest.postNotifications(boxId, correlationId, "message"))

      result mustBe notificationId
    }
  }
}
