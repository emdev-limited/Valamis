package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LUser
import com.arcusys.valamis.certificate.model._
import com.arcusys.valamis.certificate.model.badge._
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.model.PeriodTypes.PeriodType
import com.arcusys.valamis.model.{SkipTake, RangeResult}
import org.joda.time.DateTime

trait CertificateService {

  def create(companyId: Long, title: String, description: String): Certificate

  def getLogo(certificateId: Long): Option[Array[Byte]]

  def setLogo(certificateId: Long, name: String, content: Array[Byte])

  def update(id: Long,
    title: String,
    description: String,
    validPeriodType: String,
    validPeriodValue: Option[Int],
    isOpenBadgesIntegration: Boolean,
    shortDescription: String = "",
    companyId: Long,
    ownerId: Long,
    scope: Option[Long]): Certificate

  def getGoalsStatistic(certificateId: Long, userId: Long): GoalStatistic

  def getCourseGoalsCount(certificateId: Long): Int
  def getStatementGoalsCount(certificateId: Long): Int
  def getActivityGoalsCount(certificateId: Long): Int
  def getPackageGoalsCount(certificateId: Long): Int

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          sortAZ: Boolean,
                                          skipTake: Option[SkipTake],
                                          titlePattern: Option[String]
                                           ): RangeResult[Certificate]

  def getCertificatesByUserWithOpenBadges(userId: Long,
                                          companyId: Long,
                                          titlePattern: Option[String] = None
                                           ): Seq[Certificate]

  def getPackageGoals(certificateId: Long): Seq[PackageGoal]
  def addPackageGoal(certificateId: Long, packageId: Long): Option[PackageGoal]
  def deletePackageGoal(certificateId: Long, packageId: Long): Unit
  def changePackageGoalPeriod(certificateId: Long, packageId: Long, periodValue: Int, periodType: PeriodType): PackageGoal

  def getCourseGoals(certificateId: Long): Seq[CourseGoal]

  def getStatementGoals(certificateId: Long): Iterable[StatementGoal]

  def getActivityGoals(certificateId: Long): Iterable[ActivityGoal]

  def addCourseGoal(certificateId: Long, courseId: Long)

  def addUser(certificateId: Long, userId: Long, addActivity: Boolean = false, courseId: Option[Long] = None)

  def addActivityGoal(certificateId: Long, activityName: String, count: Int)

  def addStatementGoal(certificateId: Long, verb: String, obj: String)

  def deleteCourseGoal(certificateId: Long, courseId: Long)

  def deleteUser(certificateId: Long, userId: Long)

  def deleteActivityGoal(certificateId: Long, activityName: String)

  def deleteStatementGoal(certificateId: Long, verb: String, obj: String)

  def changeLogo(id: Long, logo: String = "")

  def changeCourseGoalPeriod(certificateId: Long, courseId: Long, v: Int, pT: PeriodType)

  def changeActivityGoalPeriod(certificateId: Long, activityName: String, count: Int, v: Int, pT: PeriodType)

  def changeStatementGoalPeriod(certificateId: Long, verb: String, obj: String, value: Int, period: PeriodType)

  def delete(id: Long)

  def getForUser(userId: Long,
                 companyId: Long,
                 sortAZ: Boolean = true,
                 skipTake: Option[SkipTake] = None,
                 titlePattern: Option[String] = None,
                 isPublished: Option[Boolean] = None
                  ): RangeResult[Certificate]

  def getSuccessByUser(userId: Long, companyId: Long, titlePattern: Option[String] = None): Seq[Certificate]

  def hasUser(certificateId: Long, userId: Long): Boolean

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

  def reorderCourseGoals(certificateId: Long, courseIds: Seq[Long])
}
