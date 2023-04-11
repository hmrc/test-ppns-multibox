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
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import play.mvc.Http.Status.OK
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.test.WireMockSupport
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId
import utils.AsyncHmrcSpec

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

    val boxName  = "box-name"
    val clientId = "client-id"
    val boxId    = "box-id"

    def stubReturns(body: String): Any =
      stubFor(
        get(urlPathEqualTo("/box"))
          .withQueryParam("boxName", equalTo(boxName))
          .withQueryParam("clientId", equalTo(clientId))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(body)
          )
      )

  }

  "getBoxId" should {

    "return a box ID if successful" in new Setup {
      stubReturns(
        s"""{
           |    "boxId": "$boxId",
           |    "boxName": "$boxName",
           |    "boxCreator": {
           |        "clientId": "$clientId"
           |    },
           |    "applicationId": "f255573c-445f-43cc-86f3-0d76122ed7b5",
           |    "clientManaged": false
           |}""".stripMargin.stripLineEnd
      )

      val result = await(underTest.getBoxId(boxName, clientId))

      result mustBe Some(BoxId(boxId))
    }

    "return None if unsuccessful" in new Setup {
      stubReturns(
        """{
          |    "code": "BOX_NOT_FOUND",
          |    "message": "Box not found"
          |}""".stripMargin.stripLineEnd
      )

      val result = await(underTest.getBoxId(boxName, clientId))

      result mustBe None
    }
  }
}
