val scala3Version = "3.0.0-RC3"

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-simple",
    version := "0.1.0",

    scalaVersion := scala3Version,

    libraryDependencies ++= Seq(
      "org.jsoup" % "jsoup" % "1.8.3",
      "io.circe" %% "circe-core" % "0.14.0-M6",
      "io.circe" %% "circe-generic" % "0.14.0-M6",
      "io.circe" %% "circe-parser" % "0.14.0-M6",
      "com.novocode" % "junit-interface" % "0.11" % "test",
    )
  )
