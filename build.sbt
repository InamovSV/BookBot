val scalaSettings = Seq(
  version := "0.1",
  scalaVersion := "2.12.6",
  addCompilerPlugin(
    "org.scalamacros" % "paradise" % "2.1.1" cross CrossVersion.full
  )
)

val circeV = "0.9.3"
val circeDeps = Seq(
  "io.circe" %% "circe-core",
  "io.circe" %% "circe-generic",
  "io.circe" %% "circe-parser",
  "io.circe" %% "circe-java8"
).map(_ % circeV)

val akkaDeps = Seq(
  "com.typesafe.akka" %% "akka-http" % "10.1.2",
  "com.typesafe.akka" %% "akka-stream" % "2.5.13",
  "de.heikoseeberger" %% "akka-http-circe" % "1.21.0"
)

val loggingDeps = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0",
  "ch.qos.logback" % "logback-classic" % "1.1.7"
)

val akkaSettings = scalaSettings ++ Seq(
  libraryDependencies ++= akkaDeps ++ loggingDeps
)

lazy val model =
  Project(id = "model", base = file("model"))
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= circeDeps
    )

lazy val repositories =
  Project(id = "repositories", base = file("repositories"))
    .dependsOn(model)
    .settings(scalaSettings: _*)
    .settings(
      libraryDependencies ++= loggingDeps ++ Seq(
        "com.typesafe.slick" %% "slick" % "3.2.3",
        "com.typesafe.slick" %% "slick-hikaricp" % "3.2.3",
        "org.postgresql" % "postgresql" % "42.2.2",
        "com.github.tminglei" %% "slick-pg" % "0.16.2"
      )
    )

lazy val client = Project(id = "client", base = file("client"))
  .dependsOn(repositories)
  .settings(akkaSettings:_*)

lazy val bot = project.in(file("bot"))
  .dependsOn(client)
  .settings(akkaSettings: _*)
  .settings(libraryDependencies ++= Seq(
    "info.mukel" %% "telegrambot4s" % "3.0.14"
  ))


lazy val root =
  Project("akka-http-workshop", file("."))
    .aggregate(bot)
