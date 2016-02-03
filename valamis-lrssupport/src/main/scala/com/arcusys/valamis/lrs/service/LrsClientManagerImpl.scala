package com.arcusys.valamis.lrs.service

import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrs.api._
import com.arcusys.valamis.lrs.api.valamis._
import com.arcusys.valamis.lrs.tincan.AuthorizationScope
import com.escalatesoft.subcut.inject.{BindingModule, Injectable}
import com.liferay.portal.kernel.log.{LogFactoryUtil, Log}
import com.liferay.portal.service.ServiceContextThreadLocal

// TODO: refactor and split this file
class LrsClientManagerImpl(implicit val bindingModule: BindingModule) extends LrsClientManager with Injectable {
  private val log = LogFactoryUtil.getLog(classOf[LrsClientManagerImpl])
  private lazy val authCredentials = inject[CurrentUserCredentials]
  private lazy val lrsRegistration = inject[LrsRegistration]


  def statementApi[T](action: (StatementApi => T), authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new StatementApi()(getLrsSettingsForLrsApi(auth))
    run(api, action)
  }

  def verbApi[T](action: VerbApi => T, authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new VerbApi()(getLrsSettingsForLrsApi(auth))
    run(api, action)
  }

  def scaleApi[T](action: ScaleApi => T, authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new  ScaleApi()(getLrsSettingsForLrsApi(auth))
    run(api, action)
  }

  def activityProfileApi[T](action: ActivityProfileApi => T, authInfo: Option[String]): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new ActivityProfileApi()(getLrsSettingsForLrsApi(auth))
    run(api, action)
  }

  def activityApi[T](action: ActivityApi => T, authInfo: Option[String] = None): T = {
    val auth = authInfo.getOrElse(getUserAuth)
    val api = new ActivityApi()(getLrsSettingsForLrsApi(auth))
    run(api, action)
  }

  private def run[T, A <: BaseApi](api: A, action: A => T): T = {
    try {
      action(api)
    }
    finally {
      api.close()
    }
  }

  private def getLrsSettingsForLrsApi(auth: String, version: String = ProxyLrsInfo.Version) = {
    val proxyUrl = PortalUtilHelper.getLocalHostUrl + ProxyLrsInfo.FullPrefix

    LrsSettings(proxyUrl, version, new LrsAuthDefaultSettings(auth))
  }

  private def getUserAuth = {
    val context = ServiceContextThreadLocal.getServiceContext
    if (context != null) {

      val request = context.getRequest
      val session = request.getSession

      val auth = authCredentials.get(session).map(_.auth)

      if (auth.isEmpty)
        log.warn("auth is empty")

      auth.getOrElse("")
    }
    else {
      lrsRegistration.getLrsEndpointInfo(AuthorizationScope.AllRead).auth
    }
  }
}
