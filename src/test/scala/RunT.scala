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
