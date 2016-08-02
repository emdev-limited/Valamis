package com.arcusys.valamis.certificate.service

import com.arcusys.learn.liferay.LiferayClasses.LSocialActivity
import com.arcusys.valamis.certificate.model.goal._
import com.arcusys.valamis.certificate.storage._
import com.arcusys.valamis.model.PeriodTypes
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.mockito.Matchers._
import org.scalatest.FunSuite

class ActivityGoalStatusCheckerComponentTest extends FunSuite {

  val stateRepository = mock(classOf[CertificateGoalStateRepository])
  when(stateRepository.create(notNull().asInstanceOf[CertificateGoalState])).thenReturn(null)

  val checker = new ActivityGoalStatusCheckerComponent {
    var certificateStateRepository: CertificateStateRepository = _
    var activityGoalStorage: ActivityGoalStorage = _
    var goalRepository: CertificateGoalRepository = _
    def goalStateRepository = stateRepository

    def checkActivityGoalPublic(userId: Long, activities: Seq[LSocialActivity], userJoinedDate: DateTime)
                               (activityGoal: ActivityGoal, goalData: CertificateGoal) = {
      this.checkActivityGoal(userId, activities, userJoinedDate)(activityGoal, goalData)
    }
  }

  test("no activities with unlimited test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq()
    val goal = ActivityGoal(1, 11, "com.activity", 1)
    val goalData = CertificateGoal(1, 11, GoalType.Activity, 0, PeriodTypes.UNLIMITED, 1, false, groupId = None)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("no activities with timeout test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq()
    val goal = ActivityGoal(2, 11, "com.activity", 1)
    val goalData = CertificateGoal(2, 11, GoalType.Activity,1, PeriodTypes.DAYS, 1, false, groupId = None)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Failed)
  }

  test("no activities with no timeout test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq()
    val goal = ActivityGoal(3, 11, "com.activity", 1)
    val goalData = CertificateGoal(3, 11, GoalType.Activity, 11, PeriodTypes.DAYS, 1, false, groupId = None)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("single activity success test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq(createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity"))
    val goal = ActivityGoal(4, 11, "com.activity", 1)
    val goalData = CertificateGoal(4, 11, GoalType.Activity, 2, PeriodTypes.DAYS, 1, false, groupId = None)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Success)
  }

  test("single activity timeout test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq(createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.activity"))
    val goal = ActivityGoal(5, 11, "com.activity", 1)
    val goalData = CertificateGoal(5, 11, GoalType.Activity, 2, PeriodTypes.DAYS, 1, false, groupId = None)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Failed)
  }

  test("single activity inprogress unlimited test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq(createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.other.activity"))
    val goal = ActivityGoal(6, 11, "com.activity", 1)
    val goalData = CertificateGoal(6, 11, GoalType.Activity, 0, PeriodTypes.UNLIMITED, 1, false, groupId = None)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("several activities success test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(7, 11, "com.activity", 1)
    val goalData = CertificateGoal(7, 11, GoalType.Activity, 2, PeriodTypes.DAYS, 1, false, groupId = None)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Success)
  }

  test("several activities success test, goals count = 2") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(8, 11, "com.activity", 2)
    val goalData = CertificateGoal(8, 11, GoalType.Activity, 5, PeriodTypes.DAYS, 1, false, groupId = None)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(8), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Success)
  }

  test("several activities inprogress test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(9, 11, "com.activity", 2)
    val goalData = CertificateGoal(9, 11, GoalType.Activity, 15, PeriodTypes.DAYS, 1, false, groupId = None)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.other.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("several activities unlimited inprogress test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(10, 11, "com.activity", 2)
    val goalData = CertificateGoal(10, 11, GoalType.Activity, 0, PeriodTypes.UNLIMITED, 1, false, groupId = None)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.other.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("several activities failed test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, 11, "com.activity", 2)
    val goalData = CertificateGoal(11, 11, GoalType.Activity, 2, PeriodTypes.DAYS, 1, false, groupId = None)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Failed)
  }

  test("several activities failed test, goals count = 2") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(12, 11, "com.activity", 2)
    val goalData = CertificateGoal(12, 11, GoalType.Activity, 5, PeriodTypes.DAYS, 1, false, groupId = None)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal, goalData)

    assert(goalStatus == GoalStatuses.Failed)
  }

  private def createSocialActivity(createdDate: DateTime, activityName: String) = {
    val activity = mock(classOf[LSocialActivity])
    when(activity.getCreateDate).thenReturn(createdDate.toDate.getTime)
    when(activity.getClassName).thenReturn(activityName)
    activity
  }
}