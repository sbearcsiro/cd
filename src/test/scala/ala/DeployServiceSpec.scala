package ala

import akka.actor.Props
import org.specs2.mutable.Specification
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

class DeployServiceSpec extends Specification with Specs2RouteTest with DeployService {
  def actorRefFactory = system // connect the DSL to the test ActorSystem

  val apiKey = "1234"
  
  val deployActor = system.actorOf(Props(classOf[NoopDeployActor]))
  
  "The service" should {

    "leave GET requests unhandled" in {
      Get("/deploy/2.3") ~> deployRoute ~> check {
        handled must beFalse
      }
    }

    "leave POST requests wihtout a version number unhandled" in {
      Post("/deploy") ~> deployRoute ~> check {
        handled must beFalse
      }
    }

    "return Forbidden to a POST request to a version without the API key header" in {
      Post("/deploy/2.3") ~> sealRoute(deployRoute) ~> check {
        status === Forbidden
      }
    }

    "return Forbidden to a POST request to a version with an invalid API key header" in {
      Post("/deploy/2.3") ~> addHeader("X-DEPLOY-KEY", "4321") ~> sealRoute(deployRoute) ~> check {
        status === Forbidden
      }
    }

    "return Accepted to a POST request to a version with a valid API key header" in {
      Post("/deploy/2.3") ~> addHeader("X-DEPLOY-KEY", apiKey) ~> deployRoute ~> check {
        status === Accepted
      }
    }

    "return a MethodNotAllowed error for PUT requests" in {
      Put("/deploy/2.3") ~> sealRoute(deployRoute) ~> check {
        status === MethodNotAllowed
        responseAs[String] === "HTTP method not allowed, supported methods: POST"
      }
    }
  }
  
}

class NoopDeployActor extends DeployActor {

  val appName = "test"
  val deploy = (version: String) => { logger.debug(s"Deploying $version") }

  def sendHipChatMessage(message: HipChatMessage) = { logger.debug(s"Sending $message"); None }
}