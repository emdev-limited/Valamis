package com.arcusys.valamis.web.configuration.ioc

import com.arcusys.learn.liferay.util.DataAccessUtil
import com.arcusys.valamis.persistence.common.{SlickDBInfo, SlickDBInfoLiferayImpl}
import com.escalatesoft.subcut.inject.MutableBindingModule

object Configuration extends MutableBindingModule {
  this <~ new WebConfiguration(inject[SlickDBInfo](None))

  this.bind[SlickDBInfo] toSingle new SlickDBInfoLiferayImpl(DataAccessUtil.getDataSource)
}