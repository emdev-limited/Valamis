package com.arcusys.valamis.hook

import com.arcusys.valamis.hook.utils.{PageLayout, Utils}
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.service._
import com.liferay.portal.util.PortalUtil

class CreateDashboardAction extends SimpleAction {
  private val log = LogFactoryUtil.getLog(classOf[CreateDashboardAction])

  private val valamisSiteName = "Liferay"
  private val valamisSiteFriendlyURL = "/guest"
  private val valamisThemeId = "valamismoonstonetheme_WAR_valamismoonstonetheme"

  private val DashboardTemplateId = "valamisStudentDashboard"
  private val dashboardLayout = PageLayout("Dashboard", templateId = DashboardTemplateId)
  private val dashboardPortlets = Map(
    "ValamisStudySummary_WAR_learnportlet" -> "valamisStudySummary",
    "MyCertificates_WAR_learnportlet" -> "learningPaths",
    "LearningPaths_WAR_learnportlet" -> "learningPaths",
    "MyCourses_WAR_learnportlet" -> "lessons",
    "MyLessons_WAR_learnportlet" -> "lessons",
    "RecentLessons_WAR_learnportlet" -> "recent",
    "AchievedCertificates_WAR_learnportlet" -> "achievedCertificates",
    "ValamisActivities_WAR_learnportlet" -> "valamisActivities"
  )

  private val gradesLayout = PageLayout(name = "Grades", templateId = "1_column")
  private val gradesPortlets = Map("Gradebook_WAR_learnportlet" -> "column-1")


  override def run(companyIds: Array[String]) {
    val companyId = PortalUtil.getDefaultCompanyId
    val userId = UserLocalServiceUtil.getDefaultUserId(companyId.toLong)
    val defaultSite = Option(GroupLocalServiceUtil.fetchFriendlyURLGroup(companyId.toLong, valamisSiteFriendlyURL))

    for (site <- defaultSite) {
      val siteId = site.getGroupId

      Utils.setupTheme(siteId, valamisThemeId)

      if (!Utils.hasPage(siteId, dashboardLayout.isPrivate, dashboardLayout.friendlyUrl)) {
        createPageWithPortlets(siteId, userId, dashboardLayout, dashboardPortlets)
      }

      if (!Utils.hasPage(siteId, gradesLayout.isPrivate, gradesLayout.friendlyUrl)) {
        createPageWithPortlets(siteId, userId, gradesLayout, gradesPortlets)
      }
    }
  }

  private def createPageWithPortlets(siteId: Long, userId: Long, layout: PageLayout, portlets: Map[String, String]): Unit = {
    log.info(s"Create ${layout.name} page")
    Utils.setupPages(siteId, userId, layout.isPrivate, Seq(layout))
    Utils.updatePortletsForLayout(siteId, layout, portlets)
  }


}