package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName}
import com.arcusys.valamis.web.portlet.base.{Permission, PermissionBase}
import com.arcusys.valamis.web.servlet.base.exceptions._
import org.apache.http.ParseException
import org.scalatra._
import org.slf4j.LoggerFactory

case class PermissionCredentials(groupId: Long, portletId: String, primaryKey: String)


class ScalatraPermissionUtil(scalatra: ScalatraBase) extends PermissionUtil {
  def getCourseIdFromRequest(implicit request: HttpServletRequest): Long = {
    Option(request.getParameter("courseId"))
      .orElse(scalatra.params.get("courseId"))
      .map(parseCourseId)
      .getOrElse(throw AccessDeniedException("courseId is empty"))
  }

}

object PermissionUtil extends PermissionUtil {
  def getCourseIdFromRequest(implicit request: HttpServletRequest): Long = {
    Option(request.getParameter("courseId")).map(parseCourseId)
      .getOrElse(throw AccessDeniedException("courseId is empty"))
  }
}

trait PermissionUtil {

  val logger = LoggerFactory.getLogger(PermissionUtil.getClass)

  def getCourseIdFromRequest(implicit request: HttpServletRequest): Long

  def requireCurrentLoggedInUser(userId: Long) = {
    if (getUserId != userId)
      throw AccessDeniedException()
  }

  def getUserId: Long = ServiceContextHelper.getServiceContext.getUserId

  def getCompanyId: Long = PermissionHelper.getPermissionChecker().getCompanyId

  def getCourseId: Long = ServiceContextHelper.getServiceContext.getRequest.getParameter("courseId").toLong

  def requireLogin() = {
    if (!isAuthenticated)
      throw new NotAuthorizedException
  }

  def getLiferayUser = UserLocalServiceHelper().getUser(PermissionHelper.getPermissionChecker().getUserId)

  def isAuthenticated: Boolean = PermissionHelper.getPermissionChecker().isSignedIn

  def hasPermissionApi(permission: PermissionBase, portlets: PortletName*)(implicit r: HttpServletRequest): Boolean = {
    hasPermissionApiSeq(PermissionHelper.getPermissionChecker(), permission, portlets)
  }

  def hasPermissionApi(user: LUser, permission: PermissionBase, portlets: PortletName*)
                      (implicit r: HttpServletRequest): Boolean = {
    hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets)
  }

  def hasPermissionApi(courseId: Long, user: LUser, permission: PermissionBase, portlets: PortletName*): Boolean = {
    hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets, courseId)
  }

  def requirePermissionApi(permission: PermissionBase, portlets: PortletName*)(implicit r: HttpServletRequest): Unit = {
    val companyId = PortalUtilHelper.getCompanyId(r)

    val user = Option(PortalUtilHelper.getUser(r)).getOrElse {
      UserLocalServiceHelper().getUser(UserLocalServiceHelper().getDefaultUserId(companyId))
    }

    if (!hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets)) {
      throw AccessDeniedException(s"no ${permission.name} permission for ${portlets.mkString(", ")}")
    }
  }

  def requirePermissionApi(permissions: Permission*)(implicit r: HttpServletRequest): Unit = {
    if (!permissions.foldLeft(false) { (acc, permission) =>
      acc || hasPermissionApiSeq(PermissionHelper.getPermissionChecker(), permission.permission, permission.portlets)
    }) throw AccessDeniedException("You don't have required permissions")
  }

  def requirePermissionApi(user: LUser, permission: PermissionBase, portlets: PortletName*)
                          (implicit r: HttpServletRequest): Unit = {
    if (!hasPermissionApiSeq(PermissionHelper.getPermissionChecker(user), permission, portlets)) {
      throw AccessDeniedException(s"no ${permission.name} permission for ${portlets.mkString(", ")}")
    }
  }

  protected def parseCourseId(raw: String): Long = {
    try {
      raw.toLong
    } catch {
      case e: NumberFormatException => throw new BadRequestException("courseId is incorrect")
    }
  }

  private def hasPermissionApiSeq(checker: LPermissionChecker,
                                  permission: PermissionBase,
                                  portlets: Seq[PortletName])
                                 (implicit r: HttpServletRequest): Boolean = {

    val keys = portlets.map(_.key)

    getCurrentLayout match {
      case Some(layout) =>
        check(checker, permission, keys, Seq(layout))
      case None =>
        val courseId = getCourseIdFromRequest
        hasPermissionApiSeq(checker, permission, portlets, courseId)
    }

  }

  private def hasPermissionApiSeq(checker: LPermissionChecker,
                                  permission: PermissionBase,
                                  portlets: Seq[PortletName],
                                  courseId: Long): Boolean = {
    val portletIds = portlets.map(_.key)

    //at first, check the permission at group/company scope
    if (portletIds.exists(hasPermission(checker, courseId, _, None, permission))) {
      true
    } else {//then look for the permission on any page of the site
      lazy val privateLayouts = LayoutLocalServiceHelper.getLayouts(courseId, privateLayout = true)
      lazy val publicLayouts = LayoutLocalServiceHelper.getLayouts(courseId, privateLayout = false)
      //TODO get rid of it - we don't need this actually, because we
      //we can use current layout. And if there is no current layout, then
      //checking by group/company can be used
      check(checker, permission, portletIds, privateLayouts) ||
        check(checker, permission, portletIds, publicLayouts)
    }
  }

  private def check(checker: LPermissionChecker, permission: PermissionBase, keys: Seq[String], allLayouts: Seq[LLayout]): Boolean = {
    for (
      layout <- allLayouts;
      plid = layout.getPlid;
      portletId <- LayoutLocalServiceHelper.getPortletIds(layout)
    ) {
      if (keys.contains(portletId)) {
        val primaryKey = plid + LLiferayPortletSession.LayoutSeparator + portletId
        if (hasPermission(checker, layout.getGroupId, portletId, Some(primaryKey), permission)) {
          return true
        }
      }
    }
    false
  }

  def hasPermission(checker: LPermissionChecker, groupId: Long, portletId: String, primaryKey: Option[String],
                    action: PermissionBase): Boolean = {
    try {
      ResourceActionLocalServiceHelper.getResourceAction(portletId, action.name)
      //if primaryKey == portletId, then it means that we want to check permission
      //in group/company scope (figured out empirically)
      checker.hasPermission(groupId, portletId, primaryKey.getOrElse(portletId), action.name)
    } catch {
      case ex: IllegalArgumentException =>
        logger.debug("Failed to check permission", ex)
        false
      case _: LNoSuchResourceActionException =>
        false
    }

  }

  private def getCurrentLayout(implicit request: HttpServletRequest): Option[LLayout] = {
    val plid = request.getParameter("plid")
    Option(plid).filter(_.nonEmpty) flatMap { id =>
      try {
        LayoutLocalServiceHelper.fetchLayout(id.toLong)
      } catch {
        case _: NumberFormatException => throw new ParseException("Bad plid value: " + id)
      }
    }
  }

}

