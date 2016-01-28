name := "spreedly-client"

scalaVersion := "2.11.7"

organization := "com.ticketfly"

version := "1.0.0"

parallelExecution in Test := false   // Prevents sbt to execute tests in parallel
fork in Test := true                 // Forks the JVM during tests to prevent sbt OOM error
publishArtifact in Test := false

scalacOptions ++= Seq(
  "-feature",
  "-deprecation",
  "-encoding", "UTF-8",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Xfuture"
)

resolvers ++= Seq(
  "Spray releases repository" at "http://repo.spray.io",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
)

val akkaVersion = "2.3.11"
val sprayVersion = "1.3.3"

libraryDependencies ++= Seq(
  "cc.protea.spreedly"  %   "spreedly"        % "0.9.2",
  "com.typesafe.akka"   %%  "akka-actor"      % akkaVersion,
  "com.typesafe.akka"   %%  "akka-testkit"    % akkaVersion   % "test",
  "com.typesafe.akka"   %%  "akka-slf4j"      % akkaVersion,
  "io.spray"            %%  "spray-http"      % sprayVersion,
  "io.spray"            %%  "spray-httpx"     % sprayVersion,
  "io.spray"            %%  "spray-can"       % sprayVersion,
  "io.spray"            %%  "spray-testkit"   % sprayVersion  % "test",
  "io.spray"            %%  "spray-util"      % sprayVersion,
  "org.specs2"          %%  "specs2"          % "2.4.2"       % "test",
  "org.scalatest"       %%  "scalatest"       % "2.2.1"       % "test"
)

// Scalastyle configuration
scalastyleFailOnError := true

//Scoverage configuration
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true
ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*javascript.*;router;.*BuildInfo;.*Reverse.*"
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 95

// Publish configuration
publishTo := {
  val tflyBuildRepo = "http://build.ticketfly.com/artifactory/"
  if (isSnapshot.value)
    Some("snapshots" at tflyBuildRepo + "libs-snapshot-local")
  else
    Some("releases"  at tflyBuildRepo + "libs-release-local")
}
credentials += Credentials(Path.userHome / ".artifactory" / ".credentials")
publishMavenStyle := true

