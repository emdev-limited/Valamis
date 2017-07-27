package com.arcusys.learn.liferay.services

import java.util
import com.arcusys.learn.liferay.LiferayClasses.LMessage
import com.liferay.portal.kernel.messaging.{Message, MessageBusUtil}
import scala.util.Try

object MessageBusHelper {

  val defaultTimeoutInMs = 15000L

  def sendSynchronousMessage(destinationName: String, data: util.HashMap[String, AnyRef]): Try[Object] = {
    val message = new Message()
    message.setValues(data)
    Try(MessageBusUtil.sendSynchronousMessage(destinationName, message))
  }

  def sendSynchronousMessage(destinationName: String, message: LMessage, timeout: Option[Long]): Object = {
    timeout.fold(MessageBusUtil.sendSynchronousMessage(destinationName, message)){ t =>
      MessageBusUtil.sendSynchronousMessage(destinationName, message, t)
    }
  }

  def sendSynchronousMessage(destinationName: String,
                             responseDestinationName: Option[String] = None,
                             values: java.util.HashMap[String, AnyRef], timeout: Option[Long] = None): Try[Object] = {
    val message = new LMessage()
    responseDestinationName.foreach(r => message.setResponseDestinationName(r.toString))
    message.setValues(values)
    Try(sendSynchronousMessage(destinationName.toString, message, timeout))
  }

  def sendAsynchronousMessage(destinationName: String, values: java.util.HashMap[String, AnyRef]): Unit = {
    val message = new LMessage()
    message.setValues(values)
    MessageBusUtil.sendMessage(destinationName, message)
  }

  def sendAsynchronousMessage(destinationName: String, message: LMessage): Unit = {
    MessageBusUtil.sendMessage(destinationName, message)
  }

  def createResponseMessage(message: LMessage): LMessage = MessageBusUtil.createResponseMessage(message)
}