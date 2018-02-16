package com.arcusys.valamis.course

import com.arcusys.learn.liferay.services._
import com.arcusys.learn.liferay.util.{PortalUtilHelper, PortletName, UserNotificationEventLocalServiceHelper}
import com.arcusys.valamis.course.util.CourseFriendlyUrlExt
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.{AllCoursesNotificationModel, CourseNotificationModel}
import com.arcusys.valamis.util.serialization.JsonHelper
import com.liferay.portal.kernel.log.{Log, LogFactoryUtil}
import com.liferay.portal.kernel.util.PrefsPropsUtil

class CourseNotificationServiceImpl extends CourseNotificationService {
  private val log: Log = LogFactoryUtil.getLog(this.getClass)

  def sendUsersAdded(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit = {
    try {
      val course = GroupLocalServiceHelper.getGroup(courseId)

      val company = CompanyLocalServiceHelper.getCompany(course.getCompanyId)
      val userIds = collectUserIds(memberIds, memberType)
      val link = getCourseUrl(course.getCourseFriendlyUrl, course.getCompanyId)
      userIds.foreach(userId => {
        UserLocalServiceHelper().fetchUser(userId).foreach { user =>
          if (PrefsPropsUtil.getBoolean(course.getCompanyId, "valamis.course.user.added.enable")) {
            EmailNotificationHelper.sendNotification(course.getCompanyId,
              userId,
              "valamisCourseUserAddedBody",
              "valamisCourseUserAddedSubject",
              Map(
                "[$COURSE_NAME$]" -> course.getDescriptiveName,
                "[$COURSE_LINK$]" -> getLink(link,
                  course.getDescriptiveName),
                "[$USER_SCREENNAME$]" -> user.getFullName,
                "[$PORTAL_URL$]" -> company.getVirtualHostname
              )
            )
          }
          prepareSendCourseNotification(userId, "added", course.getDescriptiveName, link)

        }
      })
    } catch {
      case e: Exception => log.error(e)
    }
  }


  def sendMemberJoinRequest(courseId: Long, userId: Long): Unit = {
    try {
      val course = GroupLocalServiceHelper.getGroup(courseId)
      val role = RoleLocalServiceHelper.getRole(course.getCompanyId, RoleLocalServiceHelper.RoleSiteOwner)
      val siteOwners = UserGroupRoleLocalServiceHelper.getUsersRolesByGroupAndRole(role.getRoleId, courseId)
      val link = getCourseUrl(course.getCourseFriendlyUrl, course.getCompanyId)
      val user = UserLocalServiceHelper().getUser(userId)
      siteOwners.foreach { users =>
        prepareSendAllCoursesNotification(users.getUserId, "join", course.getDescriptiveName, link, user.getFullName)

      }
    } catch {
      case e: Exception => log.error(e)
    }
  }

  private def collectUserIds(memberIds: Seq[Long], memberType: MemberTypes.Value): Seq[Long] = {
    memberType match {
      case MemberTypes.User => memberIds
      case MemberTypes.UserGroup => memberIds.flatMap(userLocalServiceHelper.getGroupUserIds).distinct
      case MemberTypes.Organization => memberIds.flatMap(userLocalServiceHelper.getOrganizationUserIds).distinct
      case MemberTypes.Role => Seq() // Do nothing
    }
  }

  private def prepareSendCourseNotification(userId: Long, messageType: String, title: String, link: String): Unit = {
    val model = CourseNotificationModel(messageType, title, link, userId)
    UserNotificationEventLocalServiceHelper.sendNotification(JsonHelper.toJson(model),
      userId,
      PortletName.MyCourses.key)
  }

  private def prepareSendAllCoursesNotification(userId: Long,
                                                messageType: String,
                                                title: String,
                                                link: String,
                                                memberName: String): Unit = {
    val model = AllCoursesNotificationModel(messageType, title, link, userId, memberName)
    UserNotificationEventLocalServiceHelper.sendNotification(JsonHelper.toJson(model),
      userId,
      PortletName.AllCourses.key)
  }

  private def getLink(link: String, name: String): String = s"""<a href="$link">$name</a>"""

  private def getCourseUrl(courseFriendlyUrl: String, companyId: Long): String = {
    PortalUtilHelper.getLocalHostUrlForCompany(companyId).concat(courseFriendlyUrl)
  }
}
