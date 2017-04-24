package com.arcusys.valamis.course

import com.arcusys.learn.liferay.services.UserLocalServiceHelper
import com.arcusys.valamis.member.model.MemberTypes

trait CourseNotificationService {
  def userLocalServiceHelper = UserLocalServiceHelper()

  def sendUsersAdded(courseId: Long, memberIds: Seq[Long], memberType: MemberTypes.Value): Unit

  def sendMemberJoinRequest(courseId: Long, userId: Long): Unit
}
