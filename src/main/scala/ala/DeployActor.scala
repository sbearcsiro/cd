package ala

import akka.actor.Actor
import akka.event.LoggingReceive
import com.typesafe.config.Config
import com.typesafe.scalalogging.StrictLogging
import spray.client.pipelining._
import spray.http.{HttpRequest, HttpResponse}
import spray.httpx.SprayJsonSupport._
import scala.concurrent.Future

import ala.HipChatJsonProtocol._

object DeployActor {
  case class DeployVersionMessage(version: String)
}

trait DeployActor extends Actor with StrictLogging {

  import DeployActor._

  val deploy: Deploy
  val appName: String

  // optional
  def receive = normal

  // optional
  /** Handles `DeployVersionMessage` requests. */
  val normal: Receive = LoggingReceive {
    case DeployVersionMessage(version) ⇒

      logger.info(s"Attempting to deploy $appName version $version")
      try {
        deploy(version)
        logger.info(s"Successfully deployed $appName version $version")
        sendHipChatMessage(HipChatMessage(color = "green", message = s"$appName $version deployed successfully", notifyUsers = true, messageFormat = "text"))
      } catch {
        case e: Exception ⇒
          logger.error(s"Exception while deploying $appName version $version", e)
          sendHipChatMessage(HipChatMessage(color = "red", message = s"$appName $version deployment FAILED!", notifyUsers = true, messageFormat = "text"))
      }
  }

  def sendHipChatMessage(message: HipChatMessage) : Option[Future[HttpResponse]]

}

class ConcreteDeployActor(config: Config) extends DeployActor {

  implicit val system = context.system
  import system.dispatcher // execution context for futures

  //val config = ConfigFactory.load()

  val appName = config.getString("deploy.app.name")

  val hipChatEnabled = config.getBoolean("deploy.hipchat.enabled")
  val hipChatBase = if (hipChatEnabled) config.getString("deploy.hipchat.base") else ""
  val hipChatApiKey = if (hipChatEnabled) config.getString("deploy.hipchat.key") else ""
  val hipChatRoomId = if (hipChatEnabled) config.getString("deploy.hipchat.room") else ""


  val deploy = new DeployImpl(
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

  def sendHipChatMessage(message: HipChatMessage) = {
    if (hipChatEnabled) {
      val pipeline: HttpRequest ⇒ Future[HttpResponse] = addHeader("Authorization", s"Bearer $hipChatApiKey") ~> sendReceive
      Some(pipeline(Post(s"$hipChatBase/v2/room/$hipChatRoomId/notification", message)))
    } else {
      None
    }
  }
}
