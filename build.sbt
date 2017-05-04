name := "akkaIngest"
organization := "at.geoheil"

scalaVersion := "2.11.8"

scalacOptions ++= Seq(
  "-target:jvm-1.8",
  "-encoding", "UTF-8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-Xfuture",
  "-Xlint:missing-interpolator",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Ywarn-dead-code",
  "-Ywarn-unused"
)

logBuffered in Test := false

val akkaVersion = "2.4.18"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
//  "com.typesafe.akka" %% "akka-http" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream-kafka" % "0.15",
  "com.typesafe.akka" %% "akka-http-core" % "10.0.5",
//  "com.typesafe.akka" %% "akka-http-jackson" % "10.0.5",
  "com.typesafe.akka" %% "akka-stream" % akkaVersion
)


run in Compile := Defaults.runTask(fullClasspath in Compile, mainClass.in(Compile, run), runner.in(Compile, run)).evaluated

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", "MANIFEST.MF") => MergeStrategy.discard
  case _ => MergeStrategy.first
}

test in assembly := {}

initialCommands in console :=
  """
    |
  """.stripMargin

mainClass := Some("at.geoheil.Wordcount")