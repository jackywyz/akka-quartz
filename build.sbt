organization := "com.blue"

name := "akka-quartz"

version := "0.1.2"

scalaVersion :="2.9.2"

libraryDependencies ++=Seq("com.typesafe.akka" % "akka-actor" % "2.0.3",
"com.typesafe.akka" % "akka-slf4j" % "2.0.3",
"ch.qos.logback" % "logback-classic" % "1.0.6" % "runtime",
"org.quartz-scheduler" % "quartz" % "2.1.6"
)
