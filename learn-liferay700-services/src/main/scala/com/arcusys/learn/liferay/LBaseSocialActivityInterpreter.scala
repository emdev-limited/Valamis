package com.arcusys.learn.liferay

import com.arcusys.learn.liferay.LiferayClasses._
import com.liferay.portal.kernel.service.UserLocalServiceUtil
import com.liferay.portal.kernel.util.ResourceBundleLoader
import com.liferay.social.kernel.model.BaseSocialActivityInterpreter

trait LBaseSocialActivityInterpreter extends BaseSocialActivityInterpreter {

  type Context = LServiceContext

  def getUser(context: Context): LUser = {
    UserLocalServiceUtil.getUser(context.getUserId)
  }

  override def getResourceBundleLoader: ResourceBundleLoader = ???
}
