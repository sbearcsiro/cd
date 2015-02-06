package ala

import spray.json._

case class HipChatMessage(color: String = "yellow", message: String, notifyUsers: Boolean = false, messageFormat: String = "html")

object HipChatJsonProtocol extends DefaultJsonProtocol {

  implicit val hipChatMessageJsonProtocol = jsonFormat(HipChatMessage, "color", "message", "notify", "message_format")
  
  /*
   * This needs to be a custom json format as far as I can tell because
   * the hipchat message contains a 'notify' field which can't be expressed
   * in a case class as it clashes with Object.notify(), otherwise
   * one could just use json4Format()
   */
//  implicit object HipChatMessageJsonFormat extends RootJsonFormat[HipChatMessage] {
//    def write(m: HipChatMessage) =
//      JsObject(("color", JsString(m.color)), ("message", JsString(m.message)), ("notify", JsBoolean(m.notifyUsers)), ("message_format", JsString(m.messageFormat)))
//
//    def read(value: JsValue) = value match {
//      case JsObject(m) => {
//        val color = string(m.getOrElse("color", JsString("yellow")))
//        val message = string(m.getOrElse("message", JsString("")))
//        val notifyUsers = bool(m.getOrElse("notify", JsFalse))
//        val messageFormat = string(m.getOrElse("message_format", JsString("html")))
//        new HipChatMessage(color = color, message = message, notifyUsers = notifyUsers, messageFormat = messageFormat)
//      }
//      case _ => deserializationError("HipChatMessage expected")
//    }
//
//    private def string(value: JsValue) = value match {
//      case JsString(s) => s
//      case _ => ""
//    }
//
//    private def bool(value: JsValue) = value match {
//      case JsBoolean(b) => b
//      case _ => false
//    }
//  }
}
