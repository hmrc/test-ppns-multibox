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

import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Configuration
import play.api.inject.guice.GuiceApplicationBuilder
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.PlayMongoRepositorySupport
import uk.gov.hmrc.testppnsmultibox.domain.models.TimedNotification
import uk.gov.hmrc.testppnsmultibox.ppns.models.{BoxId, CorrelationId}
import utils.AsyncHmrcSpec

import java.time.Instant
import java.time.temporal.ChronoUnit.{HOURS, MILLIS}

class TimedNotificationRepositoryISpec extends AsyncHmrcSpec
    with GuiceOneAppPerSuite with BeforeAndAfterEach with PlayMongoRepositorySupport[TimedNotification] {
  
  val stubConfig = Configuration(
    "mongodb.uri" -> s"mongodb://127.0.0.1:27017/test-${this.getClass.getSimpleName}"
  )

  override def fakeApplication() = GuiceApplicationBuilder()
    .configure(stubConfig)
    .build()

  override protected val repository: PlayMongoRepository[TimedNotification] = app.injector.instanceOf[TimedNotificationRepository]

  val repoUnderTest: TimedNotificationRepository = app.injector.instanceOf[TimedNotificationRepository]

  override def beforeEach(): Unit = {
    super.beforeEach()
    prepareDatabase()
  }

  val fakeBoxId         = BoxId("box-id")
  val fakeCorrelationId = CorrelationId.random
  val notifyAt          = Instant.now().truncatedTo(MILLIS)
  val timedNotification = TimedNotification(fakeBoxId, fakeCorrelationId, notifyAt)

  "insert" should {

    "create a timed notification" in {
      await(repoUnderTest.insert(timedNotification))
      val fetchedRecords = await(repoUnderTest.collection.find().toFuture())
      val fetchedBox     = fetchedRecords.head
      fetchedBox.boxId shouldBe fakeBoxId
      fetchedBox.correlationId shouldBe fakeCorrelationId
      fetchedBox.notifyAt shouldBe notifyAt
      fetchedBox.completed shouldBe false
      fetchedBox.expiresAt shouldBe notifyAt.plus(1, HOURS)
    }
  }
}
