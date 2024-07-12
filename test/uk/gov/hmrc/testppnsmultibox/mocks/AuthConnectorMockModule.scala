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

package uk.gov.hmrc.testppnsmultibox.mocks

import scala.concurrent.Future

import org.mockito.{ArgumentMatchersSugar, MockitoSugar}

import uk.gov.hmrc.auth.core.AuthProvider.StandardApplication
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals
import uk.gov.hmrc.auth.core.{AuthConnector, AuthProviders, InternalError, UnsupportedAuthProvider}

trait AuthConnectorMockModule extends MockitoSugar with ArgumentMatchersSugar {

  val mockAuthConnector = mock[AuthConnector]

  object Authorise {

    def asStandardApplication(clientId: String) =
      when(mockAuthConnector.authorise(eqTo(AuthProviders(StandardApplication)), eqTo(Retrievals.clientId))(*, *))
        .thenReturn(Future.successful(Some(clientId)))

    def asUnsupportedAuthProvider() =
      when(mockAuthConnector.authorise(eqTo(AuthProviders(StandardApplication)), eqTo(Retrievals.clientId))(*, *))
        .thenReturn(Future.failed(UnsupportedAuthProvider()))

    def withoutClientId() =
      when(mockAuthConnector.authorise(eqTo(AuthProviders(StandardApplication)), eqTo(Retrievals.clientId))(*, *))
        .thenReturn(Future.successful(None))

    def withInternalError() =
      when(mockAuthConnector.authorise(eqTo(AuthProviders(StandardApplication)), eqTo(Retrievals.clientId))(*, *))
        .thenReturn(Future.failed(InternalError()))
  }
}
