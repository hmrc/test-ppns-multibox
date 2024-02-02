import uk.gov.hmrc.DefaultBuildSettings._

val appName = "test-ppns-multibox"

scalaVersion := "2.13.12"

ThisBuild / libraryDependencySchemes += "org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always
ThisBuild / semanticdbEnabled := true
ThisBuild / semanticdbVersion := scalafixSemanticdb.revision

lazy val microservice = Project(appName, file("."))
  .enablePlugins(PlayScala, SbtDistributablesPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    majorVersion        := 0,
    libraryDependencies ++= AppDependencies(),
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
  .settings(scalafixConfigSettings(IntegrationTest))
  .settings(CodeCoverageSettings.settings: _*)
  .settings(
    scalacOptions ++= Seq(
      "-Wconf:cat=unused&src=views/.*\\.scala:s",
      "-Wconf:cat=unused&src=.*RoutesPrefix\\.scala:s",
      "-Wconf:cat=unused&src=.*Routes\\.scala:s",
      "-Wconf:cat=unused&src=.*ReverseRoutes\\.scala:s"
    )
  )
  .settings(
    routesImport ++= Seq(
      "uk.gov.hmrc.testppnsmultibox.controllers.Binders._",
      "uk.gov.hmrc.testppnsmultibox.ppns.models._"
    )
  )

commands ++= Seq(
  Command.command("run-all-tests") { state => "test" :: "it:test" :: state },

  Command.command("clean-and-test") { state => "clean" :: "compile" :: "run-all-tests" :: state },

  // Coverage does not need compile !
  Command.command("pre-commit") { state => "clean" :: "scalafmtAll" :: "scalafixAll" :: "coverage" :: "run-all-tests" :: "coverageOff" :: "coverageAggregate" :: state }
)
