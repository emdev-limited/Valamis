package com.arcusys.valamis.hook

import com.liferay.portal.{NoSuchLayoutException, NoSuchGroupException}
import com.liferay.portal.kernel.dao.orm. QueryUtil
import com.liferay.portal.kernel.events.SimpleAction
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.model._
import com.liferay.portal.service._
import com.liferay.portal.service.permission.PortletPermissionUtil
import scala.collection.JavaConverters._
import scala.util.Try

class CreateDashboardAction extends SimpleAction {
  private val _log: Log = LogFactoryUtil.getLog(classOf[CreateDashboardAction])

  private val valamisSiteName = "Valamis"
  private val valamisSiteFriendlyURL = "/valamis"
  private val dashboardLayoutName = "Dashboard"
  private val dashboardLayoutFriendlyURL = "/dashboard"
  private val isDashboardLayoutPrivate = false

  override def run(companyIds: Array[String]) {

    companyIds.foreach { companyId =>
      val defaultUserId = UserLocalServiceUtil.getDefaultUserId(companyId.toLong)
      val valamisSite = Option(GroupLocalServiceUtil.fetchGroup(companyId.toLong, valamisSiteName))

      updateSite(defaultUserId, valamisSite)
    }
  }

  private def updateSite(userId: Long, site: Option[Group]) {
    if (site.isDefined) {
      _log.info(s"Update existing Valamis site with id: ${site.get.getGroupId}")

      val dashboardLayout = try {
        LayoutLocalServiceUtil.getFriendlyURLLayout(
          site.get.getGroupId,
          isDashboardLayoutPrivate,
          dashboardLayoutFriendlyURL
        )
      }
      catch {
        case e: NoSuchLayoutException => {
          addLayout(site.get.getGroupId, userId)
        }
      }
      addPortletsToDashboard(dashboardLayout)
    }
    else {
      _log.info("Create Valamis site with dashboard")

      val newSite = addSite(userId)
      val newSiteId = newSite.getGroupId

      setupTheme(newSiteId)

      setupDashboardPage(newSiteId, userId)

      val userIds = UserLocalServiceUtil.getUsers(QueryUtil.ALL_POS, QueryUtil.ALL_POS)
        .asScala
        .map(_.getUserId)
        .toArray

      val roles = RoleLocalServiceUtil.getTypeRoles(RoleConstants.TYPE_SITE).asScala
      val memberRole = roles.filter(role => role.getName.equals(RoleConstants.SITE_MEMBER)).head

      UserGroupRoleLocalServiceUtil.addUserGroupRoles(userIds, newSiteId, memberRole.getRoleId)
    }
  }

  private def setupTheme(siteGroupId: Long): LayoutSet = {
    val valamisThemeId = "valamistheme_WAR_valamistheme"

    LayoutSetLocalServiceUtil
      .updateLookAndFeel(siteGroupId, false, valamisThemeId, "", "", false)

    LayoutSetLocalServiceUtil
      .updateLookAndFeel(siteGroupId, true, valamisThemeId, "", "", false)
  }

  private def addSite(userId: Long): Group = {
    val groupType = GroupConstants.TYPE_SITE_OPEN
    val parentGroupId = GroupConstants.DEFAULT_PARENT_GROUP_ID
    val liveGroupId = GroupConstants.DEFAULT_LIVE_GROUP_ID
    val membershipRestriction = GroupConstants.DEFAULT_MEMBERSHIP_RESTRICTION
    val description = ""
    val manualMembership = true
    val isSite = true
    val isActive = true

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    GroupLocalServiceUtil.addGroup(
      userId,
      parentGroupId,
      classOf[Group].getName,
      0, //classPK
      liveGroupId,
      valamisSiteName,
      description,
      groupType,
      manualMembership,
      membershipRestriction,
      valamisSiteFriendlyURL,
      isSite,
      isActive,
      serviceContext)
  }

  private def setupDashboardPage(siteGroupId: Long, userId: Long) {
    _log.info("Create dashboard page")

    val dashboardLayout = addLayout(siteGroupId, userId)

    try {
      removeLayout(siteGroupId, false, "/home")
    } catch {
      case e:Throwable =>
    }

    val layoutTypePortlet = dashboardLayout.getLayoutType.asInstanceOf[LayoutTypePortlet]
    layoutTypePortlet.setLayoutTemplateId(userId, "valamisStudentDashboard")

    updateLayout(dashboardLayout)

    addPortletsToDashboard(dashboardLayout)
  }

  private def removeLayout(siteGroupId: Long, isPrivate: Boolean, friendlyUrl: String) {
    val homeLayout = LayoutLocalServiceUtil.getFriendlyURLLayout(siteGroupId, isPrivate, friendlyUrl)
    LayoutLocalServiceUtil.deleteLayout(homeLayout)
  }

  private def addLayout(siteGroupId: Long, userId: Long): Layout = {

    val serviceContext = new ServiceContext
    serviceContext.setAddGuestPermissions(true)

    val parentLayout = LayoutConstants.DEFAULT_PARENT_LAYOUT_ID
    val title = ""
    val description = ""
    val layoutType = LayoutConstants.TYPE_PORTLET

    LayoutLocalServiceUtil.addLayout(
      userId,
      siteGroupId,
      isDashboardLayoutPrivate,
      parentLayout,
      dashboardLayoutName,
      title,
      description,
      layoutType,
      false, //hidden
      dashboardLayoutFriendlyURL,
      serviceContext
    )

  }

  private def addPortletsToDashboard(dashboardLayout:Layout) = {
    addPortletId(dashboardLayout, "ValamisStudySummary_WAR_learnportlet",   "valamisStudySummary")
    addPortletId(dashboardLayout, "MyCertificates_WAR_learnportlet",        "learningPaths")
    addPortletId(dashboardLayout, "LearningPaths_WAR_learnportlet",         "learningPaths")
    addPortletId(dashboardLayout, "MyCourses_WAR_learnportlet",             "lessons")
    addPortletId(dashboardLayout, "MyLessons_WAR_learnportlet",             "lessons")
    addPortletId(dashboardLayout, "RecentLessons_WAR_learnportlet",         "recent")
    addPortletId(dashboardLayout, "AchievedCertificates_WAR_learnportlet",  "achievedCertificates")
    addPortletId(dashboardLayout, "ValamisActivities_WAR_learnportlet",     "valamisActivities")
  }

  protected def addPortletId(layout: Layout, portletId: String, columnId: String) = {
    val layoutTypePortlet = layout.getLayoutType.asInstanceOf[LayoutTypePortlet]

    if (!layoutTypePortlet.hasPortletId(portletId)) {
      val newPortletId = layoutTypePortlet.addPortletId(0, portletId, columnId, -1, false)

      addResources(layout, newPortletId)
      updateLayout(layout)
    }
  }

  protected def addResources(layout: Layout, portletId: String) {
    val rootPortletId = PortletConstants.getRootPortletId(portletId)
    val portletPrimaryKey = PortletPermissionUtil.getPrimaryKey(layout.getPlid, portletId)

    ResourceLocalServiceUtil.addResources(
      layout.getCompanyId,
      layout.getGroupId,
      0, //userId
      rootPortletId,
      portletPrimaryKey,
      true,
      true,
      true)
  }

  protected def updateLayout(layout: Layout) {
    LayoutLocalServiceUtil.updateLayout(layout.getGroupId, layout.isPrivateLayout, layout.getLayoutId, layout.getTypeSettings)
  }
}