package ala

import akka.actor.{ActorRef, Actor}
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.StrictLogging
import spray.http.HttpHeader
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.HttpService

trait DeployService extends HttpService with StrictLogging {
  
  val apiKey: String
  
  val deployActor: ActorRef

  def authenticate: HttpHeader ⇒ Option[String] = {
    case RawHeader(DeployService.DEPLOY_KEY_HEADER, `apiKey`) ⇒ Some(apiKey)
    case x ⇒ None
  }
  
  val deployRoute = 
    path( "deploy" / Segment ) { version ⇒
      post {
        headerValue(authenticate) { key ⇒
          deployActor ! DeployActor.DeployVersionMessage(version)
          complete(Accepted)
        } ~
        complete(Forbidden)
      }
    }

}

object DeployService {
  
  val DEPLOY_KEY_HEADER = "X-DEPLOY-KEY"
  
}

class DeployServiceActor(val deployActorPropsFactory: DeployActorPropsFactory) extends Actor with DeployService with StrictLogging {

  val config = ConfigFactory.load()
  logger.debug(s"Config loaded from ${config.origin().description()}")
  
  val deployActor = context.system.actorOf( deployActorPropsFactory(config) )

  val apiKey = config.getString("deploy.api.key")
  

  def receive = runRoute(sealRoute(deployRoute))

  def actorRefFactory = context

}
