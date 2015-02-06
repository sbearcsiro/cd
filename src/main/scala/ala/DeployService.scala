package ala

import java.io.IOException

import akka.actor.Actor
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.StrictLogging
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse, HttpHeader}
import spray.http.HttpHeaders.RawHeader
import spray.http.StatusCodes._
import spray.httpx.SprayJsonSupport._
import spray.routing.{ExceptionHandler, HttpService}

import scala.concurrent.Future

import ala.HipChatJsonProtocol._

trait DeployService extends HttpService with StrictLogging {

  val deploy: Deploy
  val apiKey: String
  val appName: String
  
  val hipChatEnabled: Boolean

  def authenticate: HttpHeader ⇒ Option[String] = {
    case RawHeader("X-DEPLOY-KEY", `apiKey`) ⇒ Some(apiKey)
    case x ⇒ None
  }

  def hipChatMessageHandler(version: String): ExceptionHandler =
    ExceptionHandler {
      case e: Exception ⇒ ctx ⇒ {
        logger.error(s"Exception while deploying $version", e)
        if (hipChatEnabled) {
          val resp = sendHipChatMessage(HipChatMessage(color = "red", message = s"$appName $version deployment FAILED!", notifyUsers = true, messageFormat = "text"))
        }
        ctx.complete(InternalServerError)
      }
    }
  
  val deployRoute = 
    path( "deploy" / Segment ) { version ⇒
      post {
        headerValue(authenticate) { key ⇒
          handleExceptions(hipChatMessageHandler(version)) {
            logger.info(s"Attempting to deploy version $version")
            deploy(version)
            logger.info(s"Successfully deployed version $version")
            if (hipChatEnabled) {
              val resp = sendHipChatMessage(HipChatMessage(color = "green", message = s"$appName $version deployed successfully", notifyUsers = true, messageFormat = "text"))
            }
            complete(NoContent)
          }
        } ~
        complete(Forbidden)
      }
    }
  
  def sendHipChatMessage(message: HipChatMessage) : Future[HttpResponse]
  
}

class DeployServiceActor extends Actor with DeployService with StrictLogging {

  implicit val system = context.system
  import system.dispatcher // execution context for futures
  
  val config = ConfigFactory.load()
  logger.debug(s"Config loaded from ${config.origin().description()}")
  val apiKey = config.getString("deploy.api.key")
  val appName = config.getString("deploy.app.name")

  val hipChatEnabled = config.getBoolean("deploy.hipchat.enabled")
  val hipChatBase = config.getString("deploy.hipchat.base")
  val hipChatApiKey = config.getString("deploy.hipchat.key")
  val hipChatRoomId = config.getString("deploy.hipchat.room")
  
  val deploy = new Deploy(
    group = config.getString("deploy.group"),
    appName = appName,
    dbName = config.getString("deploy.db.name"),
    dbHost= config.getString("deploy.db.host"),
    dbUsername = config.getString("deploy.db.username"),
    dbPassword = config.getString("deploy.db.password"),
    snapshot = config.getString("deploy.snapshot.regex").r,
    downloadUrl = config.getString("deploy.download.url"),
    downloadDir = config.getString("deploy.download.dir"),
    backupDir = config.getString("deploy.backup.dir"),
    catalinaBase = config.getString("deploy.catalina.base"),
    catalinaWebapps = config.getString("deploy.catalina.webapps"),
    webappContext = config.getString("deploy.webapp.context")
  )
  
  def receive = runRoute(deployRoute)

  def actorRefFactory = context

  def sendHipChatMessage(message: HipChatMessage) : Future[HttpResponse] = {
    val pipeline: HttpRequest ⇒ Future[HttpResponse] = addHeader("Authorization", s"Bearer $hipChatApiKey") ~> sendReceive
    pipeline(Post(s"$hipChatBase/v2/room/$hipChatRoomId/notification",message))
  }
}