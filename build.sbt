organization := "com.blue"

name := "akka-quartz"

version := "0.1.0-SNAPSHOT"


libraryDependencies ++=Seq("com.typesafe.akka" % "akka-actor" % "2.0.2",
"org.quartz-scheduler" % "quartz" % "2.1.6"
)
