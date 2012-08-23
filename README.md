##copyright
core scala code(QuartzActor.scala) forking at [theatrus/akka-quartz](https://github.com/theatrus/akka-quartz)
##Using

Include the following dependency in your `build.sbt`:

    "com.blue" % "akka-quartz_2.9.2" % "0.1.2"

Include the following resolver in your `build.sbt`:
    
    resolvers +="blue repo" at "http://blueway.github.com/repo/release"

config the cron expression in your `application.conf`:
   
```properties
    quartz{
     job.cron="0/15 * * * * ?"
     job1.cron="0/15 * * * * ?"
     job2.cron="0/15 * * * * ?"
     threadPool.threadCount="2" # it is the same as prop:("org.quartz.threadPool.threadCount")
     jobStore.class=""
     scheduler.skipUpdateCheck=""
     props{
       threadPool-threadCount=3 # it is the same as prop:("org.quartz.threadPool.threadCount")
       jobStore-isClustered = true
       jobStore-clusterCheckinInterval = 2000
     }
    }
```

config cluster:
   [quartz-cluster-config-ref](http://quartz-scheduler.org/documentation/quartz-2.x/configuration/ConfigJDBCJobStoreClustering)
  
config the following start parameter if jar runnig:
   
    -Dconfig.resource=/application.conf

coding demo:

```scala
import akka.actor._
class PrintActor extends Actor{
  def receive ={
    case Message(msg) => println(msg)
  }
}

case class Message(msg:String)

object RunT{

  import com.blue._

  def main(args: Array[String]) {
   val system = ActorSystem("sys") 
   val quartzActor = system.actorOf(Props[QuartzActor])
   val dest= system.actorOf(Props[PrintActor])
  //default jobname is "job"
  quartzActor ! AddCronSchedule(dest, "0/5 * * * * ?", Message("hello"))
  quartzActor ! AddCronSchedule(dest, "0/15 * * * * ?", Message("world"),"job1")
  }

}

```

##Log config
add the logback.xml file at the directory of `src/main/resources`
and add following configs in:  

```xml
  <?xml version="1.0" encoding="UTF-8"?>
  <configuration>

    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <target>System.out</target>
        <encoder>
            <pattern>%date{MM/dd HH:mm:ss} %-5level[%thread] %logger{1} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- <appender name="FILE" class="ch.qos.logback.core.FileAppender">
        <file>quartz.log</file>
        <append>false</append>
        <encoder>
            <pattern>%date{MM/dd HH:mm:ss} %-5level[%thread] %logger{1} - %msg%n</pattern>
        </encoder>
    </appender> -->

    <logger name="akka" level="DEBUG" />

    <root level="DEBUG">
        <appender-ref ref="CONSOLE"/>
        <!--<appender-ref ref="FILE"/>-->
    </root>

</configuration>
```


##TODO
