package com.arcusys.valamis.web.servlet.certificate.response

import com.arcusys.valamis.model.PeriodTypes
import com.arcusys.valamis.user.model.UserInfo
import com.arcusys.valamis.web.servlet.course.CourseResponse
import org.joda.time.DateTime

case class CertificateResponse(id: Long,
                               title: String,
                               shortDescription: String,
                               description: String,
                               logo: String,
                               isActive: Boolean,
                               periodType: PeriodTypes.PeriodType,
                               periodValue: Int,
                               createdAt: DateTime,
                               isOpenBadgesIntegration: Boolean,
                               courses: Iterable[CourseGoalResponse],
                               statements: Iterable[StatementGoalResponse],
                               activities: Iterable[ActivityGoalResponse],
                               packages: Iterable[PackageGoalResponse],
                               assignments: Iterable[AssignmentGoalResponse],
                               users: Map[String, UserInfo],
                               scope: Option[CourseResponse],
                               isJoint: Option[Boolean] = None,
                               userStatus: Option[String] = None,
                               goalGroups: Seq[GoalGroupResponse]) extends CertificateResponseContract {
  def expirationDate = PeriodTypes.getEndDate(periodType, periodValue, createdAt)
}