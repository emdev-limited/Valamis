package com.arcusys.valamis.web.servlet.base

import javax.servlet.http.HttpServletRequest

import com.arcusys.learn.liferay.LiferayClasses._
import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName}
import com.arcusys.valamis.web.portlet.base.{Permission, PermissionBase}
import com.arcusys.valamis.web.servlet.base.exceptions.{AccessDeniedException, NotAuthorizedException}
import org.scalatra._
import org.slf4j.LoggerFactory

case class PermissionCredentials(groupId: Long, portletId: String, primaryKey: String)



class ScalatraPermissionUtil(scalatra: ScalatraBase) extends PermissionUtil {
  def getCourseIdFromRequest(implicit request: HttpServletRequest): Long = {
    Option(request.getParameter("courseId")).map(_.toLong)
      .orElse(scalatra.params.get("courseId").map(_.toLong))
      .getOrElse(throw AccessDeniedException("courseId is empty"))
  }

}

object PermissionUtil extends PermissionUtil {
  def getCourseIdFromRequest(implicit request: HttpServletRequest): Long = {
    Option(request.getParameter("courseId")).map(_.toLong)
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

  def getLiferayUser = UserLocalServiceHelper().fetchUser(PermissionHelper.getPermissionChecker().getUserId)

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

  def requirePermissionApi(permission: PermissionBase, portlets: PortletName*)(implicit r: HttpServletRequest):Unit = {
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

  private def hasPermissionApiSeq(checker: LPermissionChecker,
                                  permission: PermissionBase,
                                  portlets: Seq[PortletName])
                                 (implicit r: HttpServletRequest): Boolean = {

    val keys = portlets.map(_.key)
    val courseId = getCourseIdFromRequest
    val plidLayouts = getPlid.map(LayoutLocalServiceHelper.getLayout)

    // getGroupId uses Liferay session attribute USER_ID,
    // this attribute exists after login.
    val groupId = if (checker.isSignedIn) Some(getGroupId) else None

    hasPermissionApiSeq(checker, permission, portlets, courseId) || plidLayouts.fold(false)(l => check(checker, permission, keys, Seq(l), groupId))
  }

  private def hasPermissionApiSeq(checker: LPermissionChecker,
                                  permission: PermissionBase,
                                  portlets: Seq[PortletName],
                                  courseId: Long): Boolean = {

    val keys = portlets.map(_.key)
    lazy val privateLayouts = LayoutLocalServiceHelper.getLayouts(courseId, true)
    lazy val publicLayouts = LayoutLocalServiceHelper.getLayouts(courseId, false)

    checker.isGroupAdmin(courseId) ||
      check(checker, permission, keys, privateLayouts) ||
      check(checker, permission, keys, publicLayouts)

  }

  /**
    * Returns true, if the user is admin, or if he/she has the desired permission
    * on the given portlet on any page of the given group.
    * */
  private def hasGroupPermissionApiSeq(checker: LPermissionChecker,
                                       permission: PermissionBase,
                                       portlets: Seq[PortletName],
                                       courseId: Option[Int] = None): Boolean = {
    val keys = portlets.map(_.key)

    val groupId = courseId.getOrElse(0)

    lazy val privateLayouts = LayoutLocalServiceHelper.getLayouts(groupId, true)
    lazy val publicLayouts = LayoutLocalServiceHelper.getLayouts(groupId, false)

    checker.isGroupAdmin(groupId) ||
      check(checker, permission, keys, privateLayouts) ||
      check(checker, permission, keys, publicLayouts)
  }

  private def check(checker: LPermissionChecker, permission: PermissionBase, keys: Seq[String], allLayouts: Seq[LLayout], groupId: Option[Long] = None): Boolean = {

    for (
      layout <- allLayouts;
      plid = layout.getPlid;
      portletId <- LayoutLocalServiceHelper.getPortletIds(layout)
    ) {
      if (keys.contains(portletId)) {
        val primaryKey = plid + LLiferayPortletSession.LayoutSeparator + portletId
        val groupIdnew = groupId.getOrElse(layout.getGroupId)
        if (hasPermission(checker, groupIdnew, portletId, primaryKey, permission)) {
          return true
        }
      }
    }
    false
  }

  def hasPermission(groupId: Long, portletId: String, primaryKey: String, action: PermissionBase): Boolean = {
    hasPermission(PermissionHelper.getPermissionChecker(), groupId, portletId, primaryKey, action)
  }

  def hasPermission(checker: LPermissionChecker, groupId: Long, portletId: String, primaryKey: String, action: PermissionBase): Boolean = {
    try {
      ResourceActionLocalServiceHelper.getResourceAction(portletId, action.name)
      checker.hasPermission(groupId, portletId, primaryKey, action.name)
    } catch {
      // TODO: init resource permission on deploy, LR7(servlet)
      case e: IllegalArgumentException => false
      case _: LNoSuchResourceActionException => false
    }

  }

  private def getPlid(implicit request: HttpServletRequest): Option[Long] = {
    val plid = request.getParameter("plid")
      Option(plid).filter(_.nonEmpty).map(_.toLong)
    }

  private def getGroupId(implicit request: HttpServletRequest) = {
    val user =  request.getAttribute("USER_ID")
    UserLocalServiceHelper().getUser(user.asInstanceOf[Long]).getGroupId
  }
}

