###copyright###
core scala code forking at [theatrus/akka-quartz](https://github.com/theatrus/akka-quartz)
###Using###

Include the following dependency in your `build.sbt`:

    "com.blue" % "akka-quartz_2.9.2" % "0.1.0"

Include the following resolver in your `build.sbt`:
    
    resolvers +="blue repo" at "http://blueway.github.com/repo/release"

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
  quartzActor ! AddCronSchedule(dest, "0/5 * * * * ?", Message("hello"))
  }

}

```
