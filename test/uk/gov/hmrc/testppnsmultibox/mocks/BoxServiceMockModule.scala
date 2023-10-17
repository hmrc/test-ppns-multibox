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

import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId
import uk.gov.hmrc.testppnsmultibox.ppns.services.BoxService

trait BoxServiceMockModule extends MockitoSugar with ArgumentMatchersSugar {

  val mockBoxService = mock[BoxService]

  object GetBoxId {

    def returns(boxId: BoxId) =
      when(mockBoxService.getBoxId(*)(*)).thenReturn(Future.successful(Some(boxId)))

    def returnsNoBox() =
      when(mockBoxService.getBoxId(*)(*)).thenReturn(Future.successful(None))

    def verifyCalledWith(clientId: String) =
      verify(mockBoxService).getBoxId(eqTo(clientId))(*)
  }

  object ValidateBoxOwnership {

    def returns(valid: Boolean) =
      when(mockBoxService.validateBoxOwnership(*[BoxId], *)(*)).thenReturn(Future.successful(valid))

    def verifyCalledWith(boxId: BoxId, clientId: String) =
      verify(mockBoxService).validateBoxOwnership(eqTo(boxId), eqTo(clientId))(*)
  }
}
