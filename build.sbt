organization := "com.blue"

name := "akka-quartz"

version := "0.1.0"

scalaVersion :="2.9.2"

libraryDependencies ++=Seq("com.typesafe.akka" % "akka-actor" % "2.0.3",
"com.typesafe.akka" % "akka-testkit" % "2.0.3",
"org.specs2" % "specs2_2.9.2" % "1.12" % "test",
"org.quartz-scheduler" % "quartz" % "2.1.6"
)
