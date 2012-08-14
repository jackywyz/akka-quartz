###Using###

Include the following dependency in your `build.sbt`:

    "com.blue" % "akka-quartz_2.9.2" % "0.1.0"

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
