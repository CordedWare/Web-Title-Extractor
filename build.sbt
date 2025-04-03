ThisBuild / scalaVersion := "3.2.2"

ThisBuild / version      := "0.1.0-SNAPSHOT"

lazy val extractor       = "web.title.extractor"

lazy val scala3Version   = "3.2.1"

lazy val circeVersion               = "0.14.0"
lazy val catsEffectVersion          = "3.3.14"
lazy val http4sVersion              = "0.23.15"
lazy val doobieVersion              = "1.0.0-RC1"
lazy val pureConfigVersion          = "0.17.1"
lazy val log4catsVersion            = "2.4.0"
lazy val tsecVersion                = "0.4.0"
lazy val scalaTestVersion           = "3.2.12"
lazy val scalaTestCatsEffectVersion = "1.4.0"
lazy val testContainerVersion       = "1.17.3"
lazy val logbackVersion             = "1.4.0"
lazy val slf4jVersion               = "2.0.0"
lazy val jsoupVersion               = "1.15.3"
lazy val ip4sVersion                = "3.3.0"
lazy val redis4catsVersion          = "1.4.3"

lazy val server = (project in file("."))
    // .enablePlugins(JavaAppPackaging)
    .settings(
        name         := "web-title-extractor-service",
        scalaVersion := scala3Version,
        organization := extractor,
        libraryDependencies ++= Seq(
            "org.typelevel"         %% "cats-effect"                   % catsEffectVersion,
            "org.http4s"            %% "http4s-client"                 % http4sVersion,
            "org.http4s"            %% "http4s-dsl"                    % http4sVersion,
            "org.http4s"            %% "http4s-ember-server"           % http4sVersion,
            "org.http4s"            %% "http4s-ember-client"           % http4sVersion,
            "org.http4s"            %% "http4s-circe"                  % http4sVersion,
            "io.circe"              %% "circe-generic"                 % circeVersion,
            "io.circe"              %% "circe-fs2"                     % circeVersion,
            "com.github.pureconfig" %% "pureconfig-core"               % pureConfigVersion,
            "org.typelevel"         %% "log4cats-slf4j"                % log4catsVersion,
            "org.slf4j"              % "slf4j-simple"                  % slf4jVersion,
            "io.github.jmcardon"    %% "tsec-http4s"                   % tsecVersion,
            "org.typelevel"         %% "log4cats-noop"                 % log4catsVersion            % Test,
            "org.scalatest"         %% "scalatest"                     % scalaTestVersion           % Test,
            "org.typelevel"         %% "cats-effect-testing-scalatest" % scalaTestCatsEffectVersion % Test,
            "org.testcontainers"     % "testcontainers"                % testContainerVersion       % Test,
            "org.testcontainers"     % "postgresql"                    % testContainerVersion       % Test,
            "org.jsoup"              % "jsoup"                         % jsoupVersion,
            "com.comcast"           %% "ip4s-core"                     % ip4sVersion,
            "dev.profunktor"        %% "redis4cats-effects"            % redis4catsVersion,
            "dev.profunktor"        %% "redis4cats-core"               % redis4catsVersion,
            "org.typelevel"         %% "cats-effect-testkit"           % catsEffectVersion          % Test,
            "org.scalatest"         %% "scalatest"                     % "3.2.12"                   % Test
        ),
    )
