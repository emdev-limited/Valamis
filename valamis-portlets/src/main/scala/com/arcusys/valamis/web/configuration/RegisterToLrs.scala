package com.arcusys.valamis.web.configuration

import com.arcusys.learn.liferay.LiferayClasses.{LMessage, LMessageListener}
import com.arcusys.learn.liferay.LogFactoryHelper
import com.arcusys.learn.liferay.services.CompanyLocalServiceHelper
import com.arcusys.learn.liferay.services.MessageBusHelper._
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.arcusys.valamis.lrsEndpoint.model.{AuthType, LrsEndpoint}
import com.arcusys.valamis.lrsEndpoint.service.LrsEndpointService
import com.arcusys.valamis.util.serialization.JsonHelper
import com.arcusys.valamis.web.configuration.ioc.Configuration
import com.arcusys.valamis.web.service.LiferayContext
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}

import scala.util.{Failure, Success}


class LrsDeployedMessageListener extends LMessageListener {
  val logger = LogFactoryHelper.getLog(getClass)

  override def receive(message: LMessage): Unit = {
    LiferayContext.init()

    if ("deployed".equals(message.get("lrs"))) {
      logger.info("'Lrs deployed' message received")

      new LrsRegistrator()(Configuration).register()
    }
  }
}

class LrsRegistrator(implicit val bindingModule: BindingModule) extends Injectable {
  val logger = LogFactoryHelper.getLog(getClass)

  val LrsRegistration = "valamis/lrs/registration"

  protected lazy val endpointService = inject[LrsEndpointService]
  private val AppName = "VALAMIS"
  private val AppDescription = "VALAMIS"

  def register(): Unit = {

    CompanyLocalServiceHelper
      .getCompanies
      .foreach { company =>
        val companyId = company.getCompanyId

        if (endpointService.getEndpoint(companyId).isDefined) {
          logger.info("Lrs already registered, companyId: " + companyId)
        }
        else {
          logger.info("Send lrs registration message")

          val messageValues = new java.util.HashMap[String, AnyRef]()
          messageValues.put("appName", AppName)
          messageValues.put("appDescription", AppDescription)
          messageValues.put("authScope", AuthorizationScope.All.toStringParameter)
          messageValues.put("authType", "oauth")

          sendSynchronousMessage(LrsRegistration, timeout = Some(defaultTimeoutInMs), values = messageValues) match {
            case Success(null) => logger.info("Lrs not found")
            case Success("None") => logger.info("Lrs registration fail")
            case Success(body: String) =>
              val data = JsonHelper.fromJson[ResponseModel](body)
              endpointService.setEndpoint(LrsEndpoint(
                data.endpoint,
                AuthType.INTERNAL,
                data.appId,
                data.appSecret,
                Some("http://localhost:8080")))(companyId)
              logger.info("Lrs registration success")
            case Success(value) => throw new Exception(s"Unsupported response: $value")
            case Failure(ex) => throw new Exception(ex)
          }
        }
      }
  }
}

case class ResponseModel(appId: String, appSecret: String, endpoint: String)
