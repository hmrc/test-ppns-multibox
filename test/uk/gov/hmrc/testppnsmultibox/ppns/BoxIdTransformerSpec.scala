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

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

import utils.HmrcSpec

import play.api.mvc.AnyContent
import play.api.mvc.Results.Ok
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import uk.gov.hmrc.testppnsmultibox.config.AppConfig
import uk.gov.hmrc.testppnsmultibox.mocks.PushPullNotificationConnectorMockModule
import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

class BoxIdTransformerSpec extends HmrcSpec with PushPullNotificationConnectorMockModule {

  trait Setup {
    implicit val hc = HeaderCarrier()

    val mockAppConfig = mock[AppConfig]

    val apiContext = "some-context"
    val apiVersion = "some-version"
    when(mockAppConfig.apiContext).thenReturn(apiContext)
    when(mockAppConfig.apiVersion).thenReturn(apiVersion)

    val underTest = new BoxIdTransformer(mockAppConfig, mockPushPullNotificationConnector)

    val clientId    = "client-id"
    val fakeBoxId   = BoxId.random
    val fakeRequest = FakeRequest()
  }

  val requestToResultBlock = (r: RequestWithBoxId[AnyContent]) => Future.successful(Ok(r.boxId.value.toString))

  "transform" should {
    "return the box id when the client ID exists and the box was created" in new Setup {
      GetBoxId.returnsBoxId(fakeBoxId)

      val result = underTest.invokeBlock(fakeRequest.withHeaders("X-Client-ID" -> clientId), requestToResultBlock)

      status(result) shouldBe OK
      contentAsString(result) shouldBe fakeBoxId.value.toString
      GetBoxId.verifyCalledAtLeastOnceWith(s"$apiContext##$apiVersion##callbackUrl", clientId)
    }

    "return an error message when the client ID is missing" in new Setup {
      intercept[MissingClientIdException] {
        await(underTest.invokeBlock(fakeRequest, requestToResultBlock))
      }
    }

    "throw an exception when the box does not exist" in new Setup {
      GetBoxId.returnsNoBox()

      intercept[MissingBoxException] {
        await(underTest.invokeBlock(fakeRequest.withHeaders("X-Client-ID" -> clientId), requestToResultBlock))
      }

      GetBoxId.verifyCalledAtLeastOnceWith(s"$apiContext##$apiVersion##callbackUrl", clientId)
    }
  }
}
