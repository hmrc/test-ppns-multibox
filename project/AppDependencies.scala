import sbt._

object AppDependencies {

  def apply() : Seq[ModuleID] = compile ++ test

  private val bootstrapVersion = "8.4.0"
  private val hmrcMongoVersion = "1.7.0"

  val compile = Seq(
    "uk.gov.hmrc"             %% "bootstrap-backend-play-30"  % bootstrapVersion,
    "org.typelevel"           %% "cats-core"                  % "2.9.0",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-play-30"         % hmrcMongoVersion
  )

  val test = Seq(
    "uk.gov.hmrc"             %% "bootstrap-test-play-30"     % bootstrapVersion,
    "org.mockito"             %% "mockito-scala-scalatest"    % "1.17.30",
    "uk.gov.hmrc.mongo"       %% "hmrc-mongo-test-play-30"    % hmrcMongoVersion
  ).map(_ % "test")
}
