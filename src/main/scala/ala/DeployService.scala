package ala

import akka.actor.{ActorRef, Actor}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.routing.{RequestContext, HttpService}

trait DeployService extends HttpService with StrictLogging {
  
  val apiKey: String
  
  val deployActor: ActorRef

  def customDeployHeader = (ctx: RequestContext) => {
    ctx.request.headers.exists {
      case RawHeader(DeployService.DEPLOY_KEY_HEADER, `apiKey`) => true
      case _ => false
    }
  }

  val deployRoute = 
    path( "deploy" / Segment ) { version ⇒
      post {
        authorize(customDeployHeader) {
          complete {
            logger.debug(s"Authorized")
            deployActor ! DeployActor.DeployVersionMessage(version)
            Accepted
          }
        }
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
