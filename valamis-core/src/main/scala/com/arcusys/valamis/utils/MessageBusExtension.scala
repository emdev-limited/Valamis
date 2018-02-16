package com.arcusys.valamis.utils

import com.arcusys.learn.liferay.services.MessageBusHelper.sendSynchronousMessage
import org.json4s.JValue
import org.json4s.jackson.JsonMethods

import scala.util.{Failure, Success, Try}

trait MessageBusExtension extends JsonMethods {

  def prepareMessageData(data: Map[String, String]): java.util.HashMap[String, AnyRef] = {
    val messageValues = new java.util.HashMap[String, AnyRef]()
    data.keys.map(k => messageValues.put(k, data(k)))

    messageValues
  }

  def handleMessageResponse[T](response: Try[Object], convert: (String) => T): Option[T] = {
    response match {
      case Success(json: String) =>
        if (json.nonEmpty) {
          Some(convert(json))
        } else {
          None
        }
      case Success(value) => throw new Exception(s"Unsupported response: $value")
      case Failure(ex) => throw new Exception(ex)
    }
  }

  def sendMessage(destinationName: String, data: Map[String, String]): Option[JValue] = {
    val messageValues = prepareMessageData(data)
    handleMessageResponse(
      sendSynchronousMessage(destinationName, messageValues),
      s => parse(s)
    )
  }
}
