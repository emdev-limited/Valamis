package com.arcusys.valamis.web.portlet

import javax.portlet.{GenericPortlet, RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.ClassNameLocalServiceHelper
import com.arcusys.valamis.settings.storage.ActivityToStatementStorage
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}

class SocialActivitiesTinCanMapperView extends GenericPortlet with PortletBase {
  lazy val activityToStatementStorage = inject[ActivityToStatementStorage]

  override def doView(request: RenderRequest, response: RenderResponse) {

    implicit val out = response.getWriter

    val themeDisplay = LiferayHelpers.getThemeDisplay(request)
    val language = LiferayHelpers.getLanguage(request)
    val courseId = themeDisplay.getLayout.getGroupId

    val activityToVerb = Seq(
      "com.liferay.portlet.blogs.model.BlogsEntry",
      "com.liferay.portlet.documentlibrary.model.DLFileEntry",
      "com.liferay.portlet.wiki.model.WikiPage",
      "com.liferay.portlet.messageboards.model.MBMessage",
      "com.liferay.calendar.model.CalendarBooking",
      "com.liferay.portlet.bookmarks.model.BookmarksEntry"
    ).map(className => {
        Map(
          "className" -> className,
          "verb" -> activityToStatementStorage
            .getBy(courseId, ClassNameLocalServiceHelper.getClassNameId(className))
            .map(_.verb))
      })

    val translations = getTranslation("socialActivitiesMapper", language)
    val data = Map(
      "contextPath" -> getContextPath(request),
      "activityToVerb" -> activityToVerb) ++ translations

    sendTextFile("/templates/2.0/social_activities_mapper_templates.html")
    sendMustacheFile(data, "social_activities_mapper.html")
  }
}

