package com.arcusys.learn.liferay

import com.arcusys.learn.ioc.Configuration
import com.arcusys.learn.utils.LiferayContext
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.util.serialization.JsonHelper
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.kernel.messaging.{Message, MessageBusUtil, MessageListener}
import com.liferay.portal.security.auth.CompanyThreadLocal

import scala.util.{Failure, Success, Try}

class RegisterToLrs extends SimpleAction {
  override def run(companyIds: Array[String]): Unit = {
    LiferayContext.init(companyIds.head.toLong)

    new LrsRegistrator()(Configuration).register()
  }
}

class LrsDeployedMessageListener extends MessageListener {
  val logger = LogFactoryUtil.getLog(getClass)

  override def receive(message: Message): Unit = {
    LiferayContext.init()

    if ("deployed".equals(message.get("lrs"))) {
      logger.info("'Lrs deployed' message received")

      new LrsRegistrator()(Configuration).register()
    }
  }
}

class LrsRegistrator(implicit val bindingModule: BindingModule) extends Injectable {
  val logger = LogFactoryUtil.getLog(getClass)

  protected lazy val endpointService = inject[LrsEndpointService]
  private val AppName = "VALAMIS"
  private val AppDescription = "VALAMIS"

  def register(): Unit = {

    if (endpointService.getEndpoint.isDefined) {
      logger.info("Lrs already registered, companyId: " + CompanyThreadLocal.getCompanyId)
    }
    else {
      logger.info("Send lrs registration message")

      val message = new Message()
      message.put("appName", AppName)
      message.put("appDescription", AppDescription)
      message.put("authScope", AuthorizationScope.All.toStringParameter)
      message.put("authType", "oauth")

      Try {
        MessageBusUtil.sendSynchronousMessage("valamis/lrs/registration", message)
      } match {
        case Success(null) => logger.info("Lrs not found")
        case Success("None") => logger.info("Lrs registration fail")
        case Success(body: String) =>
          val data = JsonHelper.fromJson[ResponseModel](body)
          endpointService.setEndpoint(LrsEndpoint(
            data.endpoint,
            AuthType.INTERNAL,
            data.appId,
            data.appSecret))
          logger.info("Lrs registration success")
        case Success(value) => throw new Exception(s"Unsupported response: $value")
        case Failure(ex) => throw new Exception(ex)
      }
    }
  }
}

case class ResponseModel(appId: String, appSecret: String, endpoint: String)
