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

import java.util.UUID
import scala.util.control.Exception.allCatch

import play.api.Logger
import play.api.mvc.PathBindable

import uk.gov.hmrc.testppnsmultibox.ppns.models.BoxId

object Binders {
  val logger = Logger("binders")

  private def boxIdFromString(text: String): Either[String, BoxId] = {
    allCatch.opt(BoxId(UUID.fromString(text)))
      .toRight({
        logger.info("Cannot parse parameter %s as BoxId".format(text))
        "Box ID is not a UUID"
      })
  }

  implicit def boxIdPathBindable(implicit textBinder: PathBindable[String]): PathBindable[BoxId] = new PathBindable[BoxId] {

    override def bind(key: String, value: String): Either[String, BoxId] = {
      textBinder.bind(key, value).flatMap(boxIdFromString)
    }

    override def unbind(key: String, boxId: BoxId): String = {
      boxId.value.toString
    }
  }
}
