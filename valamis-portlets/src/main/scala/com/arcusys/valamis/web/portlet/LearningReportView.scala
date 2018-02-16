package com.arcusys.valamis.web.portlet

import javax.portlet.{RenderRequest, RenderResponse}

import com.arcusys.learn.liferay.services.CompanyHelper
import com.arcusys.learn.liferay.util.PortalUtilHelper
import com.arcusys.valamis.lrssupport.oauth.OAuthPortlet
import com.arcusys.valamis.web.portlet.base.{LiferayHelpers, PortletBase}
import com.arcusys.valamis.web.portlet.util.LearningReportSettingsService

class LearningReportView extends OAuthPortlet with PortletBase {

  override def doView(request: RenderRequest, response: RenderResponse) {
    implicit val themeDisplay = LiferayHelpers.getThemeDisplay(request)

    val settingsService = new LearningReportSettingsService(themeDisplay)

    val settingsLessons = settingsService.getSettingsLessons

    val settingsPaths = settingsService.getSettingsPaths

    implicit val out = response.getWriter

    sendTextFile("/templates/learning_report_templates.html")
    sendTextFile("/templates/common_templates.html")
    sendTextFile("/templates/paginator.html")
    sendTextFile("/templates/user_select_templates.html")

    sendMustacheFile(Map(
        "contextPath" -> PortalUtilHelper.getPathContext(request),
        "settingsId" -> settingsService.settingsId,
        "courseId" -> settingsLessons.courseId,
        "lessonState" -> settingsLessons.status.id,
        "questionOnly" -> settingsLessons.questionOnly,
        "lessonIds" -> settingsLessons.lessonIds.mkString(", "),
        "userIds" -> settingsLessons.userIds.mkString(", "),
        "hasSummary" -> settingsLessons.hasSummary,
        "pathsCourseId" -> settingsPaths.courseId,
        "pathsState" -> settingsPaths.status.id,
        "pathsGoalType" -> settingsPaths.goalType.id,
        "pathsCertificateIds" -> settingsPaths.certificateIds.mkString(", "),
        "pathsUserIds" -> settingsPaths.userIds.mkString(", "),
        "pathsHasSummary" -> settingsPaths.hasSummary
      ),
      "learning_report.html"
    )
  }
}