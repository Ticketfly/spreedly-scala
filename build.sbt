name := "spreedly-scala"

version := "1.0"

scalaVersion := "2.11.7"

organization := "com.ticketfly"

version := "1.0.0-SNAPSHOT"

parallelExecution in Test := false   // Prevents sbt to execute tests in parallel
fork in Test := true                 // Forks the JVM during tests to prevent sbt OOM error
publishArtifact in Test := false

// Scalastyle configuration
scalastyleFailOnError := true

//Scoverage configuration
ScoverageSbtPlugin.ScoverageKeys.coverageFailOnMinimum := true
ScoverageSbtPlugin.ScoverageKeys.coverageExcludedPackages := ".*javascript.*;router;.*BuildInfo;.*Reverse.*"
ScoverageSbtPlugin.ScoverageKeys.coverageMinimum := 95

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

libraryDependencies ++= Seq(
  "cc.protea.spreedly"  % "spreedly"        % "0.9.2",
  "com.typesafe.akka"   %%  "akka-actor"    % "2.3.6",
  "com.typesafe.akka"   %%  "akka-testkit"  % "2.3.6"   % "test",
  "com.typesafe.akka"   %% "akka-slf4j"     % "2.3.6",
  "io.spray"            %%  "spray-http"    % "1.3.3",
  "io.spray"            %%  "spray-httpx"    % "1.3.3",
  "io.spray"            %%  "spray-can"     % "1.3.3",
  "io.spray"            %%  "spray-testkit" % "1.3.3",
  "io.spray"            %%  "spray-util"    % "1.3.3",
  "org.specs2"          %% "specs2"         % "2.4.2"   % "test",
  "org.scalatest"       %% "scalatest"      % "2.2.1"   % "test"
)
