import sbt._

object AppDependencies {

  private val bootstrapVersion = "7.15.0"
  private val hmrcMongoVersion = "1.1.0"
  private val akkaVersion      = "2.6.20" // check that this matches transitive Play dependencies

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-28"  % bootstrapVersion,
    "org.typelevel"           %% "cats-core"                  % "2.9.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-28"         % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-28"     % bootstrapVersion,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.17.12",
    "org.scalatestplus.play"  %% "scalatestplus-play"         % "5.1.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-28"    % hmrcMongoVersion,
    "com.typesafe.akka"       %% "akka-actor-testkit-typed"   % akkaVersion
  ).map(_ % "test, it")
}
