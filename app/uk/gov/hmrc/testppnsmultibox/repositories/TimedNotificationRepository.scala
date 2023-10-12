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

package uk.gov.hmrc.testppnsmultibox.repositories

import java.util.concurrent.TimeUnit.SECONDS
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

import com.mongodb.client.model.ReturnDocument
import org.mongodb.scala.model.Filters.{and, equal}
import org.mongodb.scala.model.Indexes.ascending
import org.mongodb.scala.model.Updates.{combine, set}
import org.mongodb.scala.model.{FindOneAndUpdateOptions, IndexModel, IndexOptions}

import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.{Codecs, PlayMongoRepository}

import uk.gov.hmrc.testppnsmultibox.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId, NotificationId}

@Singleton
class TimedNotificationRepository @Inject() (mongo: MongoComponent)(implicit val ec: ExecutionContext)
    extends PlayMongoRepository[TimedNotification](
      collectionName = "timedNotification",
      mongoComponent = mongo,
      domainFormat = TimedNotification.format,
      indexes = Seq(
        IndexModel(
          ascending("expiresAt"),
          IndexOptions()
            .name("expiresAtTTLIndex")
            .background(true)
            .unique(false)
            .expireAfter(0, SECONDS)
        ),
        IndexModel(
          ascending("correlationId"),
          IndexOptions()
            .name("correlationId")
            .background(true)
        )
      ),
      replaceIndexes = true
    ) {

  def insert(timedNotification: TimedNotification): Future[TimedNotification] = {
    collection.insertOne(timedNotification)
      .toFuture()
      .map(_ => timedNotification)
  }

  def complete(boxId: BoxId, correlationId: CorrelationId, notificationId: NotificationId): Future[Option[TimedNotification]] = {
    collection.findOneAndUpdate(
      filter = and(
        equal("boxId", Codecs.toBson(boxId.value)),
        equal("correlationId", Codecs.toBson(correlationId.value))
      ),
      update = combine(set("completed", Codecs.toBson(true)), set("notificationId", Codecs.toBson(notificationId.value))),
      options = FindOneAndUpdateOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
    ).headOption()
  }
}
