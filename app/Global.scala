import play.api._
import org.w3.vs.model._
import org.w3.util._

object Global extends GlobalSettings {
  
  def vsconf = org.w3.vs.Prod.configuration

  def store = vsconf.store
  def system = vsconf.system
  
  val logger = play.Logger.of("Global")
  
  override def onStart(app: Application): Unit = {
    
    val w3c = Organization(name="World Wide Web Consortium")
    
    val tgambet = User(email = "tgambet@w3.org", name = "Thomas Gambet", password = "secret", organization = w3c.id)
    val bertails = User(email = "bertails@w3.org", name = "Alexandre Bertails", password = "secret", organization = w3c.id)
    
    val job = Job(
      name = "W3C",
      creator = bertails.id,
      organization = w3c.id,
      strategy = new EntryPointStrategy(
        name="irrelevantForV1",
        entrypoint=URL("http://www.w3.org/"),
        distance=1,
        linkCheck=false,
        filter=Filter(include=Everything, exclude=Nothing)))
    
    store.putJob(job)
    store.saveUser(User(email = "tgambet@w3.org", name = "Thomas Gambet", password = "secret", organization = w3c.id))
    store.saveUser(User(email = "bertails@w3.org", name = "Alexandre Bertails", password = "secret", organization = w3c.id))
  }
  
  override def onStop(app: Application): Unit = {
    system.shutdown()
  }
  
}
