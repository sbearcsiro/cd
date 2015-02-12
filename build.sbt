organizationName := "Atlas of Living Australia"
organizationHomepage := Some(url("http://www.ala.org.au"))
organization := "au.org.ala"

name := "deploy"
version := "1.0"

homepage := Some(url("http://github.com/sbearcsiro/deploy"))
startYear := Some(2015)
description := "A simple deployment tool for tomcat based apps."
licenses += "MIT" -> url("http://opensource.org/licenses/MIT")

publishTo := {
  val nexus = "https://nexus.ala.org.au/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "content/repositories/releases")
}

credentials += Credentials("Sonatype Nexus Repository Manager", "nexus.ala.org.au", System.getenv("TRAVIS_DEPLOY_USER"), System.getenv("TRAVIS_DEPLOY_PASSWORD"))

scalaVersion := "2.11.5"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"                    %%  "spray-can"       % sprayV,
    "io.spray"                    %%  "spray-routing"   % sprayV,
    "io.spray"                    %%  "spray-client"    % sprayV,
    "io.spray"                    %%  "spray-json"      % "1.3.1",
    "com.typesafe.akka"           %%  "akka-actor"      % akkaV,
    "com.typesafe.akka"           %%  "akka-slf4j"      % akkaV,
    "com.typesafe"                %   "config"          % "1.2.1",
    "com.typesafe.scala-logging"  %%  "scala-logging"   % "3.1.0",
    "ch.qos.logback"              %   "logback-classic" % "1.0.13",
    "io.spray"                    %%  "spray-testkit"   % sprayV    % "test",
    "com.typesafe.akka"           %%  "akka-testkit"    % akkaV     % "test",
    "org.specs2"                  %%  "specs2-core"     % "2.3.13"  % "test"
  )
}

//Resolver.settings