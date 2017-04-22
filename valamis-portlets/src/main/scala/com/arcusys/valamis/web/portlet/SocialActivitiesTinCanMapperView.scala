package com.arcusys.valamis.web.portlet

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.ClassNameLocalServiceHelper
import com.arcusys.valamis.settings.storage.ActivityToStatementStorage
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}
import com.arcusys.valamis.web.servlet.certificate.response.LiferayActivity

class SocialActivitiesTinCanMapperView extends GenericPortlet with PortletBase {
  lazy val activityToStatementStorage = inject[ActivityToStatementStorage]

  override def doView(request: RenderRequest, response: RenderResponse) {

    implicit val out = response.getWriter

    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    val language = LiferayHelpers.getLanguage(request)
    val courseId = themeDisplay.getLayout.getGroupId

    val activityToVerb = LiferayActivity.socialActivities.map(activity => {
        Map(
          "className" -> activity.activityId,
          "verb" -> activityToStatementStorage
            .getBy(courseId, ClassNameLocalServiceHelper.getClassNameId(activity.activityId))
            .map(_.verb),
          "title" -> activity.title
        )
      })

    val translations = getTranslation("socialActivitiesMapper", language)
    val data = Map(
      "contextPath" -> getContextPath(request),
      "activityToVerb" -> activityToVerb) ++ translations

    sendTextFile("/templates/social_activities_mapper_templates.html")
    sendMustacheFile(data, "social_activities_mapper.html")
  }
}

