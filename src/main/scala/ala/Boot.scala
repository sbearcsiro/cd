package ala

import akka.actor.{Props, ActorSystem}
import akka.io.IO
import akka.io.Tcp.CommandFailed
import akka.pattern.ask
import akka.util.Timeout
import com.typesafe.config.{ConfigFactory, Config}
import com.typesafe.scalalogging.StrictLogging
import spray.can.Http
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Boot extends App with StrictLogging {

  logger.info("continuous delivery agent starting")
  
  val config = ConfigFactory.load()
  val port = config.getInt("deploy.port")
  
  // we need an ActorSystem to host our application in
  implicit val system = ActorSystem("on-spray-can")

  // create and start our service actor
  val service = system.actorOf(Props(classOf[DeployServiceActor]), "deploy-service")
  
  implicit val timeout = Timeout(5.minutes)

  // start a new HTTP server on port 8080 with our service actor as the handler
  val http = IO(Http)
  val f = http ? Http.Bind(service, interface = "localhost", port = port)
  f map {
    case m: CommandFailed => {
      logger.error("bind command failed, shutting down continuous delivery agent")
      system.shutdown()
    }
    case x => logger.debug(s"Got message $x")
  }
  Runtime.getRuntime.addShutdownHook(new Thread(new Runnable {
      override def run(): Unit = {
        logger.info("shutting down continuous delivery agent")
        if (!system.isTerminated) {
          logger.info("shutting down actor system")
          system.shutdown()
        }
      }
    }))
}