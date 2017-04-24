package com.arcusys.valamis.hook

import com.arcusys.valamis.hook.utils.{PageLayout, Utils}
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.LogFactoryUtil
import com.liferay.portal.service._
import com.liferay.portal.util.PortalUtil

class CreateValamisSiteAction extends SimpleAction {
  private val log = LogFactoryUtil.getLog(classOf[CreateValamisSiteAction])

  private val valamisSiteName = "Liferay"
  private val valamisSiteFriendlyURL = "/guest"
  private val valamisThemeId = "valamismoonstonetheme_WAR_valamismoonstonetheme"
  private val column1Temlate = "column-1"
  private val valamisPages = Seq(
    Page(PageLayout("Admin"), portletMap("SCORMApplicationAdmin")),
    Page(PageLayout("Lesson viewer"), portletMap("SCORMApplication")),
    Page(PageLayout("Lesson studio"), portletMap("ValamisSlidesEditor")),
    Page(PageLayout("Lesson manager"), portletMap("PackageManager")),
    Page(PageLayout("Content manager"), portletMap("ContentManager")),
    Page(PageLayout("Reports"), portletMap("ValamisReport")),
    Page(PageLayout("Curriculum"), portletMap("Curriculum")),
    Page(PageLayout("Curriculum user"), portletMap("CurriculumUser")),
    Page(PageLayout("Statement viewer"), portletMap("TinCanStatementViewer")),
    Page(PageLayout("Learning transcript"), portletMap("LearningTranscript")),
    Page(PageLayout("Course manager"), portletMap("AllCourses")),
    Page(PageLayout("Course browser"), portletMap("AllCoursesStudent"))
  )

  override def run(companyIds: Array[String]) {
    val companyId = PortalUtil.getDefaultCompanyId
    val userId = UserLocalServiceUtil.getDefaultUserId(companyId.toLong)
    val defaultSite = Option(GroupLocalServiceUtil.fetchFriendlyURLGroup(companyId.toLong, valamisSiteFriendlyURL))

    for (site <- defaultSite) {
      val siteId = site.getGroupId

      Utils.setupTheme(siteId, valamisThemeId)

      valamisPages.foreach(page =>
        if (!Utils.hasPage(siteId, page.layout.isPrivate, page.layout.friendlyUrl)) {
          createPageWithPortlets(siteId, userId, page.layout, page.portlets)
        }
      )
    }
  }

  private def createPageWithPortlets(siteId: Long, userId: Long, layout: PageLayout, portlets: Map[String, String]): Unit = {
    log.info(s"Create ${layout.name} page")
    Utils.setupPages(siteId, userId, layout.isPrivate, Seq(layout))
    Utils.updatePortletsForLayout(siteId, layout, portlets)
  }

  private def portletMap(id: String) = Map(fullPortletId(id) -> column1Temlate)

  private def fullPortletId(id: String): String = id + "_WAR_learnportlet"

  private case class Page(layout: PageLayout, portlets: Map[String, String])
}