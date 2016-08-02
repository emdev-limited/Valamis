package com.arcusys.valamis.web.portlet.base

import javax.portlet.{GenericPortlet, RenderRequest}

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.ResourceActionLocalServiceHelper
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName, PortletPermissionUtilHelper}

case class Permission(permission: PermissionBase, portlets: Seq[PortletName])

sealed abstract class PermissionBase(val name: String)

object CreateCourse extends PermissionBase("CREATE_COURSE")

object ViewPermission extends PermissionBase("VIEW")

object ExportPermission extends PermissionBase("EXPORT")

object UploadPermission extends PermissionBase("UPLOAD")

object ModifyPermission extends PermissionBase("MODIFY_ACTION")

object SharePermission extends PermissionBase("SHARE_ACTION")

object PreferencesPermission extends PermissionBase("PREFERENCES")

object PublishPermission extends PermissionBase("PUBLISH")

object SetVisiblePermission extends PermissionBase("SET_VISIBLE")

object ViewAllPermission extends PermissionBase("VIEW_ALL")

object ModifyAllPermission extends PermissionBase("MODIFY_ALL")

object WriteStatusPermission extends PermissionBase("WRITE_STATUS")

object CommentPermission extends PermissionBase("COMMENT")

object LikePermission extends PermissionBase("LIKE")

object HideStatisticPermission extends PermissionBase("HIDE_STATISTIC")

object ShowAllActivities extends PermissionBase("SHOW_ALL")

object EditThemePermission extends PermissionBase("EDIT_THEME")

object OrderPermission extends PermissionBase("ORDER_ACTION")

object FileAccessPermission extends PermissionBase("FILE_ACCESS")

object DataAccessPermission extends PermissionBase("DATA_ACCESS")

class PermissionUtil(val request: RenderRequest, portlet: GenericPortlet) {

  lazy val themeDisplay = request.getAttribute(LWebKeys.ThemeDisplay).asInstanceOf[LThemeDisplay]
  lazy val groupId = themeDisplay.getScopeGroupId
  lazy val permissionChecker = themeDisplay.getPermissionChecker

  lazy val portletId = PortalUtilHelper.getPortletId(request)
  lazy val primKey = PortletPermissionUtilHelper.getPrimaryKey(themeDisplay.getPlid, portletId)

  def hasPermission(permissionKey: String): Boolean = {
    try {
      ResourceActionLocalServiceHelper.getResourceAction(portletId, permissionKey)
      permissionChecker.hasPermission(groupId, portletId, primKey, permissionKey)
    } catch {
      case e: LNoSuchResourceActionException => false
        //LR7 can throw IllegalArgumentException on first render
        // TODO: init resource permission on deploy, LR7
      case e: IllegalArgumentException => false
    }
  }
}