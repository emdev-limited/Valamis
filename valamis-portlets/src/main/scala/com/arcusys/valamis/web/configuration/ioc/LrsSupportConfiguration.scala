package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.valamis.lrs.service._
import com.arcusys.valamis.lrs.service.util.StatementChecker
import com.arcusys.valamis.lrsEndpoint.service.{LrsEndpointService, LrsEndpointServiceImpl}
import com.arcusys.valamis.lrsEndpoint.storage.{LrsEndpointStorage, LrsTokenStorage}
import com.arcusys.valamis.persistence.common.SlickDBInfo
import com.arcusys.valamis.persistence.impl.lrs.{LrsEndpointStorageImpl, TokenRepositoryImpl}
import com.arcusys.valamis.web.service.UserCredentialsStorageImpl
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

  bind[LrsClientManager] toSingle new LrsClientManagerImpl {
    lazy val authCredentials = inject[UserCredentialsStorage](None)
    lazy val lrsRegistration = inject[LrsRegistration](None)
    lazy val lrsEndpointService = inject[LrsEndpointService](None)
    lazy val statementChecker = inject[StatementChecker](None)
  }

  bind[LrsRegistration] toSingle new LrsRegistrationImpl {
    lazy val lrsEndpointService = inject[LrsEndpointService](None)
    lazy val lrsTokenStorage = inject[LrsTokenStorage](None)
    lazy val lrsOAuthService = inject[LrsOAuthService](None)
  }

  bind[LrsOAuthService] toSingle new LrsOAuthServiceImpl {
    lazy val lrsTokenStorage = inject[LrsTokenStorage](None)
  }

  bind[UserCredentialsStorage] toSingle new UserCredentialsStorageImpl

  bind[LrsEndpointService] toSingle new LrsEndpointServiceImpl {
    lazy val endpointStorage = inject[LrsEndpointStorage](None)
  }

})
