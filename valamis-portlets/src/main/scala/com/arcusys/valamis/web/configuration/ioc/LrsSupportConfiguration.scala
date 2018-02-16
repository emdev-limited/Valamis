package com.arcusys.valamis.web.configuration.ioc

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.services.{CompanyHelper, ServiceContextHelper}
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrssupport.lrs.service._
import com.arcusys.valamis.lrssupport.lrsEndpoint.service.{LrsEndpointService, LrsEndpointServiceImpl}
import com.arcusys.valamis.lrssupport.lrsEndpoint.storage.{LrsEndpointStorage, LrsTokenStorage}
import com.arcusys.valamis.lrssupport.services.UserCredentialsStorageImpl
import com.arcusys.valamis.lrssupport.tables.{LrsEndpointStorageImpl, TokenRepositoryImpl}
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.statements.StatementChecker
import com.escalatesoft.subcut.inject.{BindingModule, NewBindingModule}

/**
  * Created by mminin on 26.02.16.
  */
class LrsSupportConfiguration(db: => SlickDBInfo)(implicit configuration: BindingModule) extends NewBindingModule(module => {
  import configuration.inject
  import module.bind

  bind[LrsTokenStorage].toSingle {
    new TokenRepositoryImpl(db.databaseDef, db.slickProfile)
  }

  bind[LrsEndpointStorage] toSingle {
    new LrsEndpointStorageImpl(db.databaseDef, db.slickProfile)
  }

  bind[LrsClientManager] toSingle new LrsClientManagerImpl{
    lazy val authCredentials = inject[UserCredentialsStorage](None)
    lazy val lrsRegistration = inject[LrsRegistration](None)
    lazy val lrsEndpointService = inject[LrsEndpointService](None)
    lazy val statementChecker = inject[StatementChecker](None)

    override def getHost(companyId: Long): String = PortalUtilHelper.getLocalHostUrl
  }

  bind[LrsRegistration] toSingle new LrsRegistrationImpl {
    lazy val lrsEndpointService = inject[LrsEndpointService](None)
    lazy val lrsTokenStorage = inject[LrsTokenStorage](None)
    lazy val lrsOAuthService = inject[LrsOAuthService](None)
  }

  bind[LrsOAuthService] toSingle new LrsOAuthServiceImpl {
    lazy val lrsTokenStorage = inject[LrsTokenStorage](None)
  }


  bind[LrsEndpointService] toSingle new LrsEndpointServiceImpl {
    lazy val endpointStorage = inject[LrsEndpointStorage](None)
  }

  bind[UserCredentialsStorage] toSingle new UserCredentialsStorageImpl(getRequest)

  def getRequest: (Unit => Option[HttpServletRequest]) = _ => {
    Option(ServiceContextHelper.getServiceContext).map { context =>
      context.getRequest
    }
  }

})
