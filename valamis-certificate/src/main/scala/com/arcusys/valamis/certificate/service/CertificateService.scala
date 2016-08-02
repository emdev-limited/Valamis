package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.badge._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.lrs.tincan.Statement
import com.arcusys.valamis.member.model.MemberTypes
import com.arcusys.valamis.model.PeriodTypes._
import com.arcusys.valamis.model.{PeriodTypes, SkipTake, RangeResult}
import org.joda.time.DateTime

trait CertificateService {

  def create(companyId: Long, title: String, description: String): Certificate

  def getLogo(certificateId: Long): Option[Array[Byte]]

  def setLogo(certificateId: Long, name: String, content: Array[Byte])

  def changeLogo(id: Long, logo: String)

  def update(id: Long,
    title: String,
    description: String,
    periodType: PeriodTypes.PeriodType,
    periodValue: Int,
    isOpenBadgesIntegration: Boolean,
    shortDescription: String = "",
    companyId: Long,
    ownerId: Long,
    scope: Option[Long],
    optionGoals: Int): Certificate

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]): RangeResult[Certificate]

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          titlePattern: Option[String] = None): Seq[Certificate]

  def getAffectedCertificateIds(statements: Seq[Statement]): Seq[Long]

  def delete(id: Long): Unit

  def getForUser(userId: Long,
                 companyId: Long,
                 sortAZ: Boolean = true,
                 skipTake: Option[SkipTake] = None,
                 titlePattern: Option[String] = None,
                 isPublished: Option[Boolean] = None): RangeResult[Certificate]

  def getSuccessByUser(userId: Long, companyId: Long, titlePattern: Option[String] = None): Seq[Certificate]

  def hasUser(certificateId: Long, userId: Long): Boolean

  def getUserStatus(certificateId: Long, userId: Long): String

  def addMembers(certificateId: Long,
                 memberIds: Seq[Long],
                 memberType: MemberTypes.Value)

  def addUserMember(certificateId: Long,
                    userId: Long,
                    courseId: Long)

  def isUserJoined(certificateId: Long, userId: Long): Boolean

  def deleteMembers(certificateId: Long,
                    memberIds: Seq[Long],
                    memberType: MemberTypes.Value): Unit

  def getAvailableForUser(userId:Long,
                          filter: CertificateFilter,
                          skipTake: Option[SkipTake],
                          sortAZ: Boolean): RangeResult[Certificate]

  def getBadgeModel(certificateId: Long, rootUrl: String): BadgeModel

  def getIssuerModel(rootUrl: String): IssuerModel

  def getIssuerBadge(certificateId: Long, liferayUserId: Long, rootUrl: String): BadgeResponse

  def getUsers(c: Certificate): Seq[(DateTime, LUser)]

  def clone(certificateId: Long): Certificate

  def publish(certificateId: Long, userId: Long, courseId : Long): Unit

  def unpublish(certificateId: Long): Unit

  def addPackageGoalState(certificateId: Long, userId: Long): Unit
  def addAssignmentGoalState(certificateId: Long, userId: Long): Unit

  def getCourseGoals(certificateId: Long): Seq[CourseGoal]
  def getActivityGoals(certificateId: Long): Iterable[ActivityGoal]
  def getStatementGoals(certificateId: Long): Iterable[StatementGoal]
  def getPackageGoals(certificateId: Long): Seq[PackageGoal]
  def getAssignmentGoals(certificateId: Long): Seq[AssignmentGoal]

  def addCourseGoal(certificateId: Long, courseId: Long): CourseGoal
  def addActivityGoal(certificateId: Long, activityName: String, count: Int): ActivityGoal
  def addStatementGoal(certificateId: Long, verb: String, obj: String): StatementGoal
  def addPackageGoal(certificateId: Long, packageId: Long): PackageGoal
  def addAssignmentGoal(certificateId: Long, assignmentId: Long): AssignmentGoal
  def getGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic

  def updateGoalIndexes(goals: Map[String, Int])
  def updateGoal(goalId: Long,
                 periodValue: Int,
                 periodType: PeriodType,
                 arrangementIndex: Int,
                 isOptional: Boolean,
                 count: Option[Int],
                 groupId: Option[Long]): CertificateGoal

  def deleteGoal(goalId: Long): Unit
  def deleteGoalGroup(groupId: Long, deletedContent: Boolean): Unit
  def updateGoalGroup(goalGroup: GoalGroup): GoalGroup
  def updateGoalsInGroup(groupId:Long, goalIds: Seq[Long]): Unit
  def createGoalGroup(certificateId: Long, count: Int, goalIds: Seq[Long]): Unit
  def getGoals(certificateId: Long): Seq[CertificateGoal]
  def getGroups(certificateId: Long): Seq[GoalGroup]
}