package ala

import spray.json._

case class HipChatMessage(color: String = "yellow", message: String, notifyUsers: Boolean = false, messageFormat: String = "html")

object HipChatJsonProtocol extends DefaultJsonProtocol {

  implicit val hipChatMessageJsonProtocol = jsonFormat(HipChatMessage, "color", "message", "notify", "message_format")

}
