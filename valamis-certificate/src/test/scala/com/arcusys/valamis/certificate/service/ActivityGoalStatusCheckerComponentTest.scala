package com.arcusys.valamis.certificate.service

import com.arcusys.valamis.certificate.model.goal.{ActivityGoal, GoalStatuses}
import com.arcusys.valamis.certificate.storage.{ActivityGoalStorage, CertificateStateRepository}
import com.arcusys.valamis.model.PeriodTypes
import com.liferay.portlet.social.model.SocialActivity
import org.joda.time.DateTime
import org.mockito.Mockito._
import org.scalatest.FunSuite

class ActivityGoalStatusCheckerComponentTest extends FunSuite {

  val checker = new ActivityGoalStatusCheckerComponent {
    var certificateStateRepository: CertificateStateRepository = _
    var activityGoalStorage: ActivityGoalStorage = _

    def checkActivityGoalPublic(userId: Long, activities: Seq[SocialActivity], userJoinedDate: DateTime)
                               (activityGoal: ActivityGoal) = {
      this.checkActivityGoal(userId, activities, userJoinedDate)(activityGoal)
    }
  }

  test("no activities with unlimited test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq()
    val goal = ActivityGoal(11, "com.activity", 1, 0, PeriodTypes.UNLIMITED)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("no activities with timeout test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq()
    val goal = ActivityGoal(11, "com.activity", 1, 1, PeriodTypes.DAYS)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Failed)
  }

  test("no activities with no timeout test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq()
    val goal = ActivityGoal(11, "com.activity", 1, 11, PeriodTypes.DAYS)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("single activity success test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq(createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity"))
    val goal = ActivityGoal(11, "com.activity", 1, 2, PeriodTypes.DAYS)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Success)
  }

  test("single activity timeout test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq(createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.activity"))
    val goal = ActivityGoal(11, "com.activity", 1, 2, PeriodTypes.DAYS)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Failed)
  }

  test("single activity inprogress unlimited test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val activities = Seq(createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.other.activity"))
    val goal = ActivityGoal(11, "com.activity", 1, 0, PeriodTypes.UNLIMITED)

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("several activities success test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, "com.activity", 1, 2, PeriodTypes.DAYS)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Success)
  }

  test("several activities success test, goals count = 2") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, "com.activity", 2, 5, PeriodTypes.DAYS)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(8), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Success)
  }

  test("several activities inprogress test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, "com.activity", 2, 15, PeriodTypes.DAYS)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.other.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("several activities unlimited inprogress test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, "com.activity", 2, 0, PeriodTypes.UNLIMITED)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.other.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.InProgress)
  }

  test("several activities failed test") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, "com.activity", 2, 2, PeriodTypes.DAYS)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Failed)
  }

  test("several activities failed test, goals count = 2") {
    val userJoinedDate = DateTime.now.minusDays(10)
    val goal = ActivityGoal(11, "com.activity", 2, 5, PeriodTypes.DAYS)

    val activities = Seq(
      createSocialActivity(createdDate = userJoinedDate.plusDays(4), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(3), "com.other.activity"),
      createSocialActivity(createdDate = userJoinedDate.plusDays(1), "com.activity")
    )

    val goalStatus = checker.checkActivityGoalPublic(1, activities, userJoinedDate)(goal)

    assert(goalStatus == GoalStatuses.Failed)
  }

  private def createSocialActivity(createdDate: DateTime, activityName: String) = {
    val activity = mock(classOf[SocialActivity])
    when(activity.getCreateDate).thenReturn(createdDate.toDate.getTime)
    when(activity.getClassName).thenReturn(activityName)
    activity
  }
}
