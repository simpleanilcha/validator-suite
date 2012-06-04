package org.w3.vs.model

import java.nio.channels.ClosedChannelException
import org.joda.time.DateTime
import org.w3.util.akkaext._
import org.w3.vs.actor.message._
import org.w3.vs.exception._
import org.w3.vs.VSConfiguration
import akka.actor._
import akka.dispatch._
import play.api.libs.iteratee._
import play.Logger
import org.w3.util._
import scalaz.Validation._
import scalaz.Scalaz._
import scalaz._

// closed with its strategy
case class Job(
    id: JobId = JobId(),
    name: String,
    createdOn: DateTime = DateTime.now,
    creatorId: UserId,
    organizationId: OrganizationId,
    strategy: Strategy)(implicit conf: VSConfiguration) {

  import conf.system
  implicit def timeout = conf.timeout
  private val logger = Logger.of(classOf[Job])
  
  lazy val (enumerator, channel) = Concurrent.broadcast[RunUpdate]
  
  def toValueObject: JobVO = 
    JobVO(id, name, createdOn, creatorId, organizationId, strategy.id)
  
  def getCreator: FutureVal[Exception, User] = 
    User.get(creatorId)
  
  def getOrganization: FutureVal[Exception, Organization] = 
    Organization.get(organizationId)
  
  def getHistory: FutureVal[Exception, Iterable[JobData]] = 
    JobData.getForJob(id)

  def getRun(implicit context: ExecutionContext): FutureVal[Throwable, Run] = 
    (PathAware(organizationsRef, path).?[Run](GetRun))

  def getLastRunAssertions: FutureVal[Exception, Iterable[Assertion]] = {
    implicit def ec = conf.webExecutionContext
    FutureVal.successful(Iterable())
  }
  
  // resource url, time fetched, warnings, errors
  def getURLArticles: FutureVal[Exception, Iterable[(URL, DateTime, Int, Int)]] = {
    Assertion.getForJob(id).map(_.groupBy(_.url).map{case (url, it) => 
      (url, 
       it.map(_.timestamp).max,
       it.count(_.severity == Warning),
       it.count(_.severity == Error)
      )
    })
  }
  
  def save(): FutureVal[Exception, Job] = 
    Job.save(this)
  
  def delete(): FutureVal[Exception, Unit] = {
    cancel()
    Job.delete(id)
  }
  
  def run(): Unit = 
    PathAware(organizationsRef, path) ! Refresh
  
  def cancel(): Unit = 
    PathAware(organizationsRef, path) ! Stop

  def on(): Unit = 
    PathAware(organizationsRef, path) ! BeProactive

  def off(): Unit = 
    PathAware(organizationsRef, path) ! BeLazy
  
  
  private val organizationsRef = system.actorFor(system / "organizations")
  private val path = system / "organizations" / organizationId.toString / "jobs" / id.toString
  def !(message: Any)(implicit sender: ActorRef = null): Unit =
    PathAware(organizationsRef, path) ! message
}

object Job {

  def get(id: JobId)(implicit conf: VSConfiguration): FutureVal[Exception, Job] = {
    implicit def ec = conf.webExecutionContext
    FutureVal.successful(play.api.Global.w3)
  }
  
  def getFor(user: UserId)(implicit conf: VSConfiguration): FutureVal[Exception, Iterable[Job]] = {
    implicit def ec = conf.webExecutionContext
    FutureVal.successful(Iterable(play.api.Global.w3))
  }
  
  def getFor(organization: OrganizationId)(implicit conf: VSConfiguration): FutureVal[Exception, Iterable[Job]] = 
    sys.error("ni")
  
  def getFor(strategy: StrategyId)(implicit conf: VSConfiguration): FutureVal[Exception, Iterable[Job]] = 
    sys.error("ni")
  
  def getCreatedBy(creator: UserId)(implicit conf: VSConfiguration): FutureVal[Exception, Iterable[Job]] = 
    sys.error("ni")
  
  def save(job: Job)(implicit conf: VSConfiguration): FutureVal[Exception, Job] = {
    implicit def ec = conf.webExecutionContext
    FutureVal.failed(new Exception("Not implemented yet"))
  }
  
  def delete(id: JobId)(implicit conf: VSConfiguration): FutureVal[Exception, Unit] = sys.error("")

}

