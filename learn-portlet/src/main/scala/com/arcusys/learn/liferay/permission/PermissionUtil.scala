package com.arcusys.learn.liferay.permission

import javax.management.ListenerNotFoundException
import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.exceptions.{AccessDeniedException, NotAuthorizedException}
import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services.{LayoutLocalServiceHelper, PermissionHelper, ResourceActionLocalServiceHelper}
import com.liferay.portal.{NoSuchLayoutSetPrototypeException, NoSuchResourceActionException}
import com.liferay.portal.kernel.portlet.LiferayPortletSession
import com.liferay.portal.model.{LayoutTypePortlet, Layout}
import com.liferay.portal.security.permission.PermissionChecker
import com.liferay.portal.service.{LayoutLocalServiceUtil, ServiceContextThreadLocal, UserLocalServiceUtil}
import com.liferay.portal.util.PortalUtil
import org.slf4j.LoggerFactory

import scala.collection.JavaConverters._

case class PermissionCredentials(groupId: Long, portletId: String, primaryKey: String)

sealed abstract class PermissionBase(val name: String)

object ViewPermission extends PermissionBase("VIEW")

object ExportPermission extends PermissionBase("EXPORT")

object UploadPermission extends PermissionBase("UPLOAD")

object ModifyPermission extends PermissionBase("MODIFY_ACTION")

object SharePermission extends PermissionBase("SHARE_ACTION")

object PublishPermission extends PermissionBase("PUBLISH")

object SetVisiblePermission extends PermissionBase("SET_VISIBLE")

object SetDefaultPermission extends PermissionBase("SET_DEFAULT")

object ViewAllPermission extends PermissionBase("VIEW_ALL")

object ModifyAllPermission extends PermissionBase("MODIFY_ALL")

object WriteStatusPermission extends PermissionBase("WRITE_STATUS")

object CommentPermission extends PermissionBase("COMMENT")

object LikePermission extends PermissionBase("LIKE")

object HideStatisticPermission extends PermissionBase("HIDE_STATISTIC")

object ShowAllActivities extends PermissionBase("SHOW_ALL")

object EditThemePermission extends PermissionBase("EDIT_THEME")

object OrderPermission extends PermissionBase("ORDER_ACTION")

case class Permission(permission: PermissionBase, portlets: Seq[PortletName])

object PermissionUtil {

  val logger = LoggerFactory.getLogger(PermissionUtil.getClass)

  def requireCurrentLoggedInUser(userId: Long) = {
    if (getUserId != userId)
      throw AccessDeniedException()
  }

  def getUserId: Long = ServiceContextThreadLocal.getServiceContext.getUserId

  def getCompanyId: Long = PermissionHelper.getPermissionChecker().getCompanyId

  def getCourseId: Long = ServiceContextThreadLocal.getServiceContext.getRequest.getParameter("courseId").toLong

  def requireLogin() = {
    if (!isAuthenticated)
      throw new NotAuthorizedException
  }

  def getLiferayUser = UserLocalServiceUtil.fetchUser(PermissionHelper.getPermissionChecker().getUserId)

  def isAuthenticated: Boolean = PermissionHelper.getPermissionChecker().isSignedIn

  def hasPermissionApi(permission: PermissionBase, portlets: PortletName*)(implicit r: HttpServletRequest): Boolean = {
    hasPermissionApiSeq(PermissionHelper.getPermissionChecker(), permission, portlets, Some(r))
  }

  def hasPermissionApi(user: LUser, permission: PermissionBase, portlets: PortletName*)
                      (implicit r: HttpServletRequest): Boolean = {
    hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets,Some(r))
  }

  def hasPermissionApi(courseId: Long, user: LUser, permission: PermissionBase, portlets: PortletName*): Boolean = {
    hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets, courseId = Some(courseId.toInt))
  }

  def requirePermissionApi(permission: PermissionBase, portlets: PortletName*)(implicit r: HttpServletRequest):Unit = {
    val companyId = PortalUtil.getCompanyId(r)

    val user = Option(PortalUtil.getUser(r)).getOrElse {
      UserLocalServiceUtil.getUser(UserLocalServiceUtil.getDefaultUserId(companyId))
    }

    if (!hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets, Some(r))) {
      throw AccessDeniedException(s"no ${permission.name} permission for ${portlets.mkString(", ")}")
    }
  }

  def requirePermissionApi(permissions: Permission*)(implicit r: HttpServletRequest): Unit = {
    if (!permissions.foldLeft(false) { (acc, permission) =>
      acc || hasPermissionApiSeq(PermissionHelper.getPermissionChecker(), permission.permission, permission.portlets, Some(r))
    }) throw AccessDeniedException("You don't have required permissions")
  }

  def requirePermissionApi(user: LUser, permission: PermissionBase, portlets: PortletName*)
                          (implicit r: HttpServletRequest): Unit = {
    if (!hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets, Some(r))) {
      throw AccessDeniedException(s"no ${permission.name} permission for ${portlets.mkString(", ")}")
    }
  }

  private def hasPermissionApiSeq(checker: PermissionChecker,
                                  permission: PermissionBase,
                                  portlets: Seq[PortletName],
                                  r: Option[HttpServletRequest] = None,
                                  courseId: Option[Int] = None): Boolean = {
    val keys = portlets.map(_.key)
    val cId = courseId.getOrElse(getCourseId(r))

    lazy val privateLayouts = LayoutLocalServiceUtil.getLayouts(cId, true).asScala
    lazy val publicLayouts = LayoutLocalServiceUtil.getLayouts(cId, false).asScala


      checker.isGroupAdmin(cId) ||
      check(checker, permission, keys, privateLayouts) ||
      check(checker, permission, keys, publicLayouts)
  }

  private def check(checker: PermissionChecker, permission: PermissionBase, keys: Seq[String], allLayouts: Seq[Layout]): Boolean = {
    for (
      layout <- allLayouts;
      plid = layout.getPlid;
      portletId <- LayoutLocalServiceHelper.getPortletIds(layout)
    ) {
      if (keys.contains(portletId)) {
        val primaryKey = plid + LiferayPortletSession.LAYOUT_SEPARATOR + portletId
        if (hasPermission(checker, layout.getGroupId, portletId, primaryKey, permission)) {
          return true
        }
      }
    }
    false
  }

  def hasPermission(groupId: Long, portletId: String, primaryKey: String, action: PermissionBase): Boolean = {
    hasPermission(PermissionHelper.getPermissionChecker(), groupId, portletId, primaryKey, action)
  }

  def hasPermission(checker: PermissionChecker, groupId: Long, portletId: String, primaryKey: String, action: PermissionBase): Boolean = {
    try {
      ResourceActionLocalServiceHelper.getResourceAction(portletId, action.name)
    } catch {
      case _: NoSuchResourceActionException => return false
    }
    checker.hasPermission(groupId, portletId, primaryKey, action.name)
  }

  def getCourseId(r: Option[HttpServletRequest]) = {
    val courseId = r.getOrElse(throw AccessDeniedException("courseId is empty")).getParameter("courseId")
    Option(courseId).getOrElse(throw AccessDeniedException("courseId is empty")).toInt
  }
}

