package com.blue
/*
Copyright 2012 Yann Ramin and jackywyz

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
import akka.actor.{Cancellable, ActorRef, Actor}
import akka.event.Logging
import org.quartz.impl.StdSchedulerFactory
import java.util.Properties
import org.quartz._
import utils.Key
import com.typesafe.config._

case class AddCronSchedule(to: ActorRef, cron: String, message: Any,jobname:String="job", reply: Boolean = false)
//case class UpdateCronSchedule(ctx:JobExecutionContext ,cron: String, reply: Boolean = false)

trait AddCronScheduleResult

case class AddCronScheduleSuccess(cancel: Cancellable) extends AddCronScheduleResult

case class AddCronScheduleFailure(reason: Throwable) extends AddCronScheduleResult

case class RemoveJob(cancel: Cancellable)

private class QuartzIsNotScalaExecutor() extends Job {
	def execute(ctx: JobExecutionContext) {
		val jdm = ctx.getJobDetail.getJobDataMap() // Really?
		val msg = jdm.get("message")
		val actor = jdm.get("actor").asInstanceOf[ActorRef]
		val self= jdm.get("self").asInstanceOf[ActorRef]
                val cron = jdm.getString("cron")
                val jobname= jdm.getString("jobname")
                val qcron = QuartzActor getCron (cron,jobname)
                actor ! msg
                if(cron != qcron){
                  val schel = ctx.getScheduler
                  schel.deleteJob(ctx.getJobDetail().getKey)
                  self ! AddCronSchedule(actor,qcron,msg) 
	      }
        }
}

object QuartzActor{

  def getCron(cron:String,jobname:String):String = {
       val config = ConfigFactory.load
       try{val qcron = config.getConfig("quartz").getString(jobname+".cron"); qcron}catch{case x:ConfigException => cron}

  }
}

class QuartzActor extends Actor {
	val log = Logging(context.system, this)

	// Create a sane default quartz scheduler
	private[this] val props = new Properties()
	props.setProperty("org.quartz.scheduler.instanceName", context.self.path.name)
	props.setProperty("org.quartz.threadPool.threadCount", "1")
	props.setProperty("org.quartz.jobStore.class", "org.quartz.simpl.RAMJobStore")
	props.setProperty("org.quartz.scheduler.skipUpdateCheck", "true")	// Whoever thought this was smart shall be shot

	val scheduler = new StdSchedulerFactory(props).getScheduler


	/**
	 * Cancellable to later kill the job. Yes this is mutable, I'm sorry.
	 * @param job
	 */
	class CancelSchedule(val job: JobKey, val trig: TriggerKey) extends Cancellable {
		var cancelled = false

		def isCancelled: Boolean = cancelled

		def cancel() {
			context.self ! RemoveJob(this)
		}

	}

	override def preStart() {
		scheduler.start()
		log.info("Scheduler started")
	}

	override def postStop() {
		scheduler.shutdown()
	}

	def receive = {
		case RemoveJob(cancel) => cancel match {
			case cs: CancelSchedule => scheduler.deleteJob(cs.job); cs.cancelled = true
			case _ => log.error("Incorrect cancelable sent")
		}
		case AddCronSchedule(to, qcron, message,jobname, reply) =>
			// Try to derive a unique name for this job
                        val cron = QuartzActor getCron (qcron,jobname)
			val jobkey = new JobKey(Key.DEFAULT_GROUP, "%X".format((to.toString() + message.toString + cron + "job").hashCode))
			val trigkey = new TriggerKey(Key.DEFAULT_GROUP, to.toString() + message.toString + cron + "trigger")

			val jd = org.quartz.JobBuilder.newJob(classOf[QuartzIsNotScalaExecutor])
			val jdm = new JobDataMap()
			jdm.put("message", message)
			jdm.put("actor", to)
			jdm.put("self", self)
			jdm.put("cron", cron)
			jdm.put("jobname", jobname)
			val job = jd.usingJobData(jdm).withIdentity(jobkey).build()

			try {
				scheduler.scheduleJob(job, org.quartz.TriggerBuilder.newTrigger().startNow()
					.withIdentity(trigkey).forJob(job)
					.withSchedule(org.quartz.CronScheduleBuilder.cronSchedule(cron)).build())

				if (reply)
					context.sender ! AddCronScheduleSuccess(new CancelSchedule(jobkey, trigkey))

			} catch { // Quartz will drop a throwable if you give it an invalid cron expression - pass that info on
				case e: Throwable =>
					log.error("Quartz failed to add a task: ", e)
					if (reply)
						context.sender ! AddCronScheduleFailure(e)

			}

                /*case UpdateCronSchedule(ctx,cron,replay)=>
                      scheduler.rescheduleJob(ctx.getTrigger().getKey, org.quartz.TriggerBuilder.newTrigger().startNow()
					.withIdentity(ctx.getTrigger().getKey).forJob(ctx.getJobDetail)
					.withSchedule(org.quartz.CronScheduleBuilder.cronSchedule(cron)).build())*/


		case _ => //
	}


}
