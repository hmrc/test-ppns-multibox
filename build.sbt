import uk.gov.hmrc.DefaultBuildSettings._

val appName = "test-ppns-multibox"
scalaVersion := "2.13.8"

ThisBuild / scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(
    majorVersion        := 0,
    targetJvm           := "jvm-11",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
  )
  .settings(
    Compile / unmanagedResourceDirectories += baseDirectory.value / "resources",
  )
  .settings(
    Test / unmanagedSourceDirectories += baseDirectory.value / "testcommon"
  )
  .configs(IntegrationTest)
  .settings(
    integrationTestSettings(),
    IntegrationTest / unmanagedSourceDirectories += baseDirectory.value / "testcommon"
  )
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=views/.*\\.scala:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
    )
  )
