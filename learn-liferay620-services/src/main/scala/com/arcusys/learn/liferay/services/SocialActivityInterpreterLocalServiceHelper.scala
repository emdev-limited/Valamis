package com.arcusys.learn.liferay.services

import com.arcusys.learn.liferay.LiferayClasses.{LServiceContext, LSocialActivity, LSocialActivityFeedEntry}
import com.liferay.portal.kernel.util.StringPool
import com.liferay.portlet.social.service.SocialActivityInterpreterLocalServiceUtil


object SocialActivityInterpreterLocalServiceHelper {

  def interpret(selector: String,
                activity: LSocialActivity,
                serviceContext: LServiceContext): LSocialActivityFeedEntry = {
    SocialActivityInterpreterLocalServiceUtil.interpret(StringPool.BLANK, activity, serviceContext)
  }
}
